package com.example.sampleandroidsdk

import com.nanorep.convesationui.structure.handlers.AccountInfoProvider
import com.nanorep.nanoengine.AccountInfo
import com.nanorep.sdkcore.utils.Completion

class SimpleAccountProvider : AccountInfoProvider {

    var accounts: MutableMap<String, AccountInfo> = mutableMapOf()
    override fun provide(info: AccountInfo, callback: Completion<AccountInfo>) {
        accounts[info.getApiKey()]?.let {
            info.getInfo().update(it.getInfo())
            info
        } ?: let {
            addAccount(info)
            callback.onComplete(info)
        }
    }

    override fun update(account: AccountInfo) {
        accounts[account.getApiKey()]?.getInfo()?.update(account.getInfo())
    }

    protected open fun addAccount(account: AccountInfo) {
        accounts[account.getApiKey()] = account
    }
}