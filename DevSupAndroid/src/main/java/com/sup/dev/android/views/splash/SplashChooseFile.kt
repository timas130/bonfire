package com.sup.dev.android.views.splash

import android.content.Context
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsPermission
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.android.views.views.ViewIcon
import java.io.File


class SplashChooseFile : SplashRecycler() {

    private var currentFolder: File? = null
    private var rootFolder: File? = null
    private var showFolders = true
    private var showFiles = true
    private var canGoInFolder = true
    private var foldersIsSelectable = false
    private var fileTypes: Array<out String> = emptyArray()
    private var onFileSelected: (File) -> Unit = {}
    private var onFolderSelected: (File) -> Unit = {}

    init {

        adapter = RecyclerCardAdapter()
        rootFolder = Environment.getExternalStorageDirectory()
        currentFolder = rootFolder

        vRecycler.layoutManager = LinearLayoutManager(view.context)
        vRecycler.itemAnimator = null
        setAdapter<SplashRecycler>(adapter as RecyclerCardAdapter)
        ToolsPermission.requestReadPermission { resetCards(rootFolder!!) }
    }

    override fun onShow() {
        super.onShow()

        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is SplashViewSheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()
    }


    private fun resetCards(file: File) {
        adapter!!.clear()
        if (file.absolutePath != rootFolder!!.absolutePath) adapter!!.add(CardBack(file))
        val files = file.listFiles()
        if (showFolders) for (f in files!!) if (f.isDirectory) adapter!!.add(CardFile(f))
        if (showFiles) for (f in files!!) if (!f.isDirectory && checkType(f)) adapter!!.add(CardFile(f))
    }

    private fun checkType(f: File): Boolean {
        for (type in fileTypes)
            if (f.name.toLowerCase().endsWith(".$type")) return true
        return false
    }

    //
    //  Setters
    //

    fun setShowFiles(showFiles: Boolean): SplashChooseFile {
        this.showFiles = showFiles
        resetCards(currentFolder!!)
        return this
    }

    fun setShowFolders(showFolders: Boolean): SplashChooseFile {
        this.showFolders = showFolders
        resetCards(currentFolder!!)
        return this
    }

    fun setCanGoInFolder(canGoInFolder: Boolean): SplashChooseFile {
        this.canGoInFolder = canGoInFolder
        resetCards(currentFolder!!)
        return this
    }

    fun setFoldersIsSelectable(foldersIsSelectable: Boolean): SplashChooseFile {
        this.foldersIsSelectable = foldersIsSelectable
        resetCards(currentFolder!!)
        return this
    }

    fun setFileTypes(vararg fileTypes: String): SplashChooseFile {
        this.fileTypes = fileTypes
        return this
    }

    fun setFolder(file: File): SplashChooseFile {
        currentFolder = file
        resetCards(file)
        return this
    }

    fun setRootFolder(file: File): SplashChooseFile {
        rootFolder = file
        resetCards(file)
        return this
    }

    fun setOnFileSelected(onFileSelected: (File) -> Unit): SplashChooseFile {
        this.onFileSelected = onFileSelected
        resetCards(currentFolder!!)
        return this
    }

    fun setOnFolderSelected(onFolderSelected: (File) -> Unit): SplashChooseFile {
        this.onFolderSelected = onFolderSelected
        resetCards(currentFolder!!)
        return this
    }

    //
    //  Card
    //

    inner class CardBack constructor(val file: File) : Card(0) {

        override fun instanceView(vParent: ViewGroup): View {
            return Settings(vParent.context)
        }

        override fun bindView(view: View) {
            super.bindView(view)
            val Settings = view as Settings
            Settings.view.setPadding(ToolsView.dpToPx(16).toInt(), 0, ToolsView.dpToPx(16).toInt(), 0)
            Settings.setIcon(R.drawable.ic_keyboard_arrow_left_white_24dp)
            Settings.setOnClickListener {setFolder(file.parentFile) }
            Settings.setTitle(file.name)
        }
    }

    private inner class CardFile constructor(private val file: File) : Card(0) {
        private var viewIcon: ViewIcon? = null

        override fun instanceView(vParent: ViewGroup): View {
            return Settings(vParent.context)
        }

        private fun getViewIcon(context: Context): ViewIcon {
            if (viewIcon == null) {
                viewIcon = ToolsView.inflate(context, R.layout.z_icon)
                viewIcon!!.setImageResource(R.drawable.ic_done_white_24dp)
                viewIcon!!.setOnClickListener {
                    onFolderSelected.invoke(file)
                    hide()
                }
            }
            return viewIcon!!
        }

        override fun bindView(view: View) {
            super.bindView(view)
            val v = view as Settings
            v.view.setPadding(ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt())
            v.setTitle(file.name)
            v.setIcon(if (file.isDirectory) R.drawable.ic_folder_white_24dp else R.drawable.ic_insert_drive_file_white_24dp)
            v.setSubView(if (foldersIsSelectable && file.isDirectory && canGoInFolder) getViewIcon(view.getContext()) else null)
            v.setLineVisible(false)

            v.setOnClickListener {
                if (file.isDirectory) {
                    if (!canGoInFolder && foldersIsSelectable) {
                        onFolderSelected.invoke(file)
                        hide()
                    } else {
                        setFolder(file)
                    }
                } else {
                    onFileSelected.invoke(file)
                    hide()
                }
            }
        }
    }

}
