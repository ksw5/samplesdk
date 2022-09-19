package com.example.sampleandroidsdk

import com.integration.core.chatId
import com.integration.core.visitorId
import com.nanorep.convesationui.structure.handlers.AccountInfoProvider
import com.nanorep.nanoengine.AccountInfo
import com.nanorep.sdkcore.utils.Completion

class AccountInfoProviderImpl : AccountInfoProvider {
    override fun provide(info: AccountInfo, callback: Completion<AccountInfo>) {
        info.getInfo().chatId

        info.getInfo().visitorId
    }

    override fun update(account: AccountInfo) {
        account.getInfo().chatId

        account.getInfo().visitorId
    }
}