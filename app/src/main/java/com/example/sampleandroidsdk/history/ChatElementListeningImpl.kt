package com.example.sampleandroidsdk

import android.content.Context
import android.util.Log
import com.example.sampleandroidsdk.data.Messages
import com.example.sampleandroidsdk.data.MessagesDatabase
import com.nanorep.convesationui.structure.elements.StorableChatElement
import com.nanorep.convesationui.structure.history.HistoryCallback
import com.nanorep.convesationui.structure.history.HistoryFetching
import com.nanorep.convesationui.utils.ElementMigration
import com.nanorep.sdkcore.utils.SystemUtil
import com.nanorep.sdkcore.utils.log
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min


open class ChatElementListeningImpl(
    var context:Context,
    override var targetId: String? = null,
    var pageSize: Int = 8
) : HistoryProvider {

    protected val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    open val fetchDispatcher: CoroutineDispatcher = Dispatchers.IO
    val messagesDao = MessagesDatabase.getInstance(context).messagesDao()

    override fun onFetch(from: Int, @HistoryFetching.FetchDirection direction: Int, callback: HistoryCallback?) {
        coroutineScope.launch {
            getHistory(from, direction) { history ->
                Log.v("History", "passing history list to callback, from = $from , size = ${history.size}")

                callback?.onReady(from, direction, history)
            }
        }
    }


    override fun onReceive(item: StorableChatElement) {

        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            targetId?.run {
                messagesDao.insert(Messages(this, item).apply {
                    inDate = Date(SystemUtil.syncedCurrentTimeMillis())

                    Log.d(
                        "history",
                        "onReceive: [inDate:${inDate}][timestamp:${getTimestamp()}][text:${textContent}]"
                    )
                })
            } ?: Log.e("history", "onReceive: targetId is null action is canceled")

        }
    }
    override fun onUpdate(id: String, item: StorableChatElement) {

        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {

            Log.d("history", "onUpdate: [id:$id] [text:${item.text.log(200)}] [status:${item.getStatus()}]")
            targetId?.run {
                messagesDao.update(this, id, item.getTimestamp(), item.getStorageKey(), item.getStatus())
            } ?: Log.e("history", "onReceive: targetId is null action is canceled")
        }
    }

    override fun onRemove(id: String) {

        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            targetId?.run {
                Log.d("history", "onRemove: [id:$id]")
                messagesDao.delete(this, id)
            } ?: Log.e("history", "onReceive: targetId is null action is canceled")
        }
    }



    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override suspend fun count(): Int {
        val result = coroutineScope.async { targetId?.let { messagesDao.count(it) } ?: 0 }
        return result.await()
    }

    open suspend fun getCount(toIndex: Int, fromIdx: Int): List<Messages> {
        return targetId?.let { messagesDao.getCount(it, toIndex, fromIdx - toIndex) }
            ?: emptyList()
    }


    suspend fun getHistory(
        startFromIdx: Int,
        direction: Int,
        onFetched: (MutableList<Messages>) -> Unit
    ) {

        var fromIdx = startFromIdx

        val fetchOlder = direction == HistoryFetching.Older
        val historySize = count()

        Log.v("history", "got history size = $historySize, fromIdx = $fromIdx")

        when {
            fromIdx == -1 -> {
                fromIdx = if (fetchOlder) historySize - 1 else 0
            }
            fetchOlder -> {
                fromIdx = max(historySize - fromIdx, 0)
            }
        }

        val toIndex = if (fetchOlder)
            max(0, fromIdx - pageSize)
        else
            min(fromIdx + pageSize, historySize - 1)

        Log.d("history", "fetching history: total = $historySize, from $toIndex to $fromIdx")

        // In order to prevent Concurrent exception:
        val accountHistory = CopyOnWriteArrayList(getCount(toIndex, fromIdx))

        try {
            //Log.v("History", accountHistory.map { "item: ${it.inDate}"}.joinToString("\n"))
            coroutineScope.launch(fetchDispatcher) { onFetched.invoke(accountHistory) }

        } catch (ex: Exception) {
            onFetched.invoke(ArrayList())
        }

    }
}

class HistoryMigrationProvider(context: Context, private var onDone:(()->Unit)? = null)
    : ChatElementListeningImpl(context), ElementMigration {

    init {
        pageSize = 20
    }

    private var fetchedChunk: List<Messages> = listOf()

    override val fetchDispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getCount(toIndex: Int, fromIdx: Int): List<Messages> {
        fetchedChunk = messagesDao.getCount(toIndex, fromIdx - toIndex)
        return fetchedChunk
    }

    override fun onReplace(from: Int, migration: Map<String, StorableChatElement>?) {
        Log.d("history", "got replace from = $from of ${migration?.size?:0} items")

        coroutineScope.launch {
            migration?.takeUnless { it.isEmpty() }?.forEach { (prevId, storable) ->
                val (groupId, inDate) = fetchedChunk.find { it.getId() == prevId }
                    ?.let { it.groupId to it.inDate } ?: null to null
                groupId?.run {
                    messagesDao.delete(this, prevId)
                    messagesDao.insert(Messages(this, storable).apply {
                        this.inDate = inDate!!
                    })
                }
            }
        }
    }

    override fun onDone() {
        this.onDone?.invoke()
    }

    override suspend fun count(): Int {
        val result = coroutineScope.async { messagesDao.count() }
        return result.await()
    }

}



