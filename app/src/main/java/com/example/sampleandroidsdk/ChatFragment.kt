package com.example.sampleandroidsdk


import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil.isFileUrl
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sampleandroidsdk.databinding.FragmentChatBinding
import com.integration.bold.boldchat.core.FormData
import com.integration.bold.boldchat.core.LanguageCallback
import com.integration.bold.boldchat.core.LanguageChangeRequest
import com.integration.bold.boldchat.visitor.api.Form
import com.integration.core.FileUploadInfo
import com.integration.core.annotations.FileType
import com.integration.core.annotations.FormType
import com.integration.core.visitorId
import com.nanorep.convesationui.bold.model.BoldAccount
import com.nanorep.convesationui.bold.ui.FormListener
import com.nanorep.convesationui.structure.controller.*
import com.nanorep.convesationui.structure.providers.ChatUIProvider
import com.nanorep.convesationui.views.adapters.BubbleContentUIAdapter
import com.nanorep.nanoengine.Account
import com.nanorep.nanoengine.AccountInfo
import com.nanorep.nanoengine.bot.BotAccount
import com.nanorep.nanoengine.model.configuration.ChatFeatures
import com.nanorep.nanoengine.model.configuration.ConversationSettings
import com.nanorep.nanoengine.model.configuration.StyleConfig
import com.nanorep.nanoengine.model.configuration.TAG
import com.nanorep.sdkcore.utils.asByteArray


open class ChatFragment : Fragment(), ChatEventListener {

    private var _binding: FragmentChatBinding? = null
    val binding get() = _binding!!

    val account = BotAccount(API_KEY, ACCOUNT_NAME, KNOWLEDGE_BASE, SERVER).apply {
        userId = ""
    }
    private val account_agent: BoldAccount = BoldAccount(LIVE_AGENT_KEY)
    private val accountInfoProviderImpl = AccountInfoProviderImpl()
    open var chatController: ChatController? = null
    private val uploadFileChooser = UploadFileChooser(this, 1024 * 1024 * 37)
    private lateinit var uploadButton: ImageButton



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Bot Chat Button**/
        binding.fabBot.setOnClickListener {
            chatController = createBotChat(account, object : ChatLoadedListener {


                override fun onComplete(result: ChatLoadResponse) {
                    Log.d("user_id", "${account.userId}")
                    if (result.error != null) {
                        onError(result.error!!)
                    } else {

                        val chatElementListeningImpl = ChatElementListeningImpl(requireActivity(), account.getGroupId())

                        chatController?.setChatElementListener(chatElementListeningImpl)

                        val chatFragment: Fragment = result.fragment!!
                        fragmentManager?.beginTransaction()?.replace(R.id.content, chatFragment)
                            ?.addToBackStack("ChatFragment")
                            ?.commit()
                    }
                }
            })!!
            account.welcomeMessage
        }

        /** Live Agent Chat Button **/

        binding.fabAgent.setOnClickListener {

            chatController = createAgentChat(account_agent, object : ChatLoadedListener {
                override fun onComplete(result: ChatLoadResponse) {
                    val chatFragment: Fragment = result.fragment!!
                    fragmentManager?.beginTransaction()?.replace(R.id.content, chatFragment)?.addToBackStack("ChatFragment")?.commit()
                }

            })




        }

    }

    /**
     * Creating the live agent chat
     */

    private fun createAgentChat(account: BoldAccount, chatLoadedListener: ChatLoadedListener?) : ChatController? {

        val chatElementListeningImpl =
            ChatElementListeningImpl(requireActivity(), account.getAgentId())

        initUploadButton()
        var formListener: FormListener? = null
        val formProvider = object : FormProvider {
            override fun presentForm(formData: FormData, callback: FormListener) {
                if (formData.formType == FormType.PreChatForm) {
                    callback.onComplete(null)
                    return
                }

                formListener = callback
                formListener!!.onComplete(formData as Form)

            }



        }

        val uiProvider = ChatUIProvider(requireContext()).apply {
            chatBackground = ContextCompat.getDrawable(requireContext(), R.drawable.flowerbg)
            chatElementsUIProvider.incomingUIProvider.apply {
                configure = { adapter: BubbleContentUIAdapter ->
                    adapter.apply {
                        setTextStyle(StyleConfig(20, Color.RED, Typeface.SANS_SERIF))
                        setAvatar(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.new_bot_avatar
                            )
                        )
                        setBackground(ContextCompat.getDrawable(requireContext(),
                            R.color.purple_500
                        ))
                    }
                }
            }

            chatElementsUIProvider.outgoingUIProvider.apply {
                configure = { adapter: BubbleContentUIAdapter ->
                    adapter.apply {
                        setTextStyle(StyleConfig(20, Color.WHITE, Typeface.SANS_SERIF))
                        setBackground(ContextCompat.getDrawable(requireContext(),
                            R.color.purple_200
                        ))
                    }
                }
            }
        }

        return ChatController.Builder(requireContext())
            .formProvider(formProvider)
            .accountProvider(accountInfoProviderImpl)
            .chatUIProvider(uiProvider)
            //.chatElementListener(chatElementListeningImpl)
            .conversationSettings(ConversationSettings()).chatEventListener(this)
            .chatEventListener(this)
            .build(account_agent, chatLoadedListener)

    }

    /**
     * Creating the Bot Chat
     */

    private fun createBotChat(
        account: AccountInfo,
        chatLoadedListener: ChatLoadedListener?
    ): ChatController? {
        val chatElementListeningImpl =
            ChatElementListeningImpl(requireActivity(), this.account.getGroupId())

        initUploadButton()
        var formListener: FormListener? = null
        val formProvider = object : FormProvider {
            override fun presentForm(formData: FormData, callback: FormListener) {
                if (formData.formType == FormType.PreChatForm) {
                    callback.onComplete(null)
                    return
                }

                formListener = callback


            }

        }
        val uiProvider = ChatUIProvider(requireContext()).apply {

            chatController?.isEnabled(ChatFeatures.FileUpload)
            chatInputUIProvider.uiConfig.showUpload = true
            chatBackground = ContextCompat.getDrawable(requireContext(), R.drawable.blurrybg)
            chatElementsUIProvider.incomingUIProvider.apply {
                configure = { adapter: BubbleContentUIAdapter ->
                    adapter.apply {

                        setTextStyle(StyleConfig(20, Color.YELLOW, Typeface.SANS_SERIF))
                        setAvatar(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.new_bot_avatar_bg2
                            )
                        )
                        setBackground(ContextCompat.getDrawable(requireContext(),
                            R.color.purple_500
                        ))
                    }
                }
            }

            chatElementsUIProvider.outgoingUIProvider.apply {
                configure = { adapter: BubbleContentUIAdapter ->
                    adapter.apply {
                        setTextStyle(StyleConfig(20, Color.WHITE, Typeface.SANS_SERIF))
                        setBackground(ContextCompat.getDrawable(requireContext(),
                            R.color.purple_200
                        ))
                    }
                }
            }
        }

        return ChatController.Builder(requireContext())
            //.chatElementListener(chatElementListeningImpl)
            .formProvider(formProvider)
            .chatUIProvider(uiProvider)
            .conversationSettings(ConversationSettings()).chatEventListener(this)
            .chatEventListener(this)
            .build(account, chatLoadedListener)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun Account.getGroupId(): String? {
            return apiKey.takeUnless { it.isBlank() }
                ?: (this as? BotAccount)?.let { "${it.account.orEmpty()}#${it.knowledgeBase}" }
        }
    }

    private fun Account.getAgentId() : String? {
        return apiKey.takeUnless { it.isBlank() }
            ?: (this as? BoldAccount)?.let { "${it.info.visitorId}" }
    }

    private fun initUploadButton() {
        uploadButton = ImageButton(requireContext()).apply {
            setOnClickListener {
                onUploadFileRequest()
            }
        }

    }

    override fun onUploadFileRequest() {

        val uploadInfo = FileUploadInfo().apply {
            type = FileType.Picture
            content = content?.inputStream()?.asByteArray()
            name = ""
            filePath = ""

        }

        uploadFileChooser.apply {
            onUploadsReady = chatController!!::onUploads
            open()
        }

    }

    override fun onUrlLinkSelected(url: String) {
        // sample code for handling given link
        try {
            Log.d(TAG, ">> got url link selection: [$url]")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (isFileUrl(url)) {
                    /* val uri = FileProvider.getUriForFile(
                        // this, BuildConfig.APPLICATION_ID + ".provider",
                         File(url)
                     )*/

                    //    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                } else {
                    data = Uri.parse(url)
                }
            }

            startActivity(intent)

        } catch (e: Exception) {
            Log.w(TAG, ">> Failed to activate link on default app: " + e.message)
            super.onUrlLinkSelected(url)
        }
    }

}




