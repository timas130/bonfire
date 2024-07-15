package com.sayzen.campfiresdk.compose

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.sup.dev.android.app.SupAndroid

class MyViewModelStoreOwner : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory =
        ViewModelProvider.NewInstanceFactory()
    override val defaultViewModelCreationExtras: CreationExtras by lazy {
        val extras = MutableCreationExtras()
        extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] = SupAndroid.activity!!.application
        extras
    }
}
