package com.example.sampleandroidsdk.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nanorep.convesationui.structure.elements.ChatElement
import com.nanorep.convesationui.structure.elements.StorableChatElement
import com.nanorep.sdkcore.model.StatementScope
import com.nanorep.sdkcore.model.StatementStatus
import com.nanorep.sdkcore.model.StatusPending
import java.util.*


@Entity
class Messages() : StorableChatElement {
    @PrimaryKey
    private var id: String = ""
    var groupId: String = ""
    @ChatElement.Companion.ChatElementType
    private var type: Int = 0
    private var timestamp: Long = 0
    override var scope = StatementScope.NanoBotScope
    var key: ByteArray = byteArrayOf()
    override var isStorageReady: Boolean = true
    var textContent: String = ""
    @NonNull
    lateinit var inDate: Date
    @ColumnInfo(name = "status", defaultValue = "-1")
    @StatementStatus
    private var status: Int = StatusPending

    constructor(groupId: String, storable: StorableChatElement) : this() {
        this.groupId = groupId
        id = storable.getId()
        type = storable.getType()
        timestamp = storable.getTimestamp()
        status = storable.getStatus()
        scope = storable.scope
        key = storable.getStorageKey()
        isStorageReady = storable.isStorageReady
        textContent = storable.text
    }


    override fun getId(): String {
        return id
    }

    override fun getStorageKey(): ByteArray {
        return key
    }

    override fun getTimestamp(): Long {
        return timestamp
    }


    override fun getType(): Int {
        return type
    }

    fun setId(id: String) {
        this.id = id
    }

    override fun getStatus(): Int {
        return status
    }

    fun setTimestamp(timestamp: Long) {
        this.timestamp = timestamp
    }

    fun setType(type: Int) {
        this.type = type
    }

    fun setStatus(@StatementStatus status: Int) {
        this.status = status
    }


}
