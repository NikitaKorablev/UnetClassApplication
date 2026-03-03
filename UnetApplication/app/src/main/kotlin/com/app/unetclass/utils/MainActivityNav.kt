package com.app.unetclass.utils

import android.content.Context
import com.app.domain.utils.Router

interface MainActivityNav: Router {
    fun toTransparencySettings(context: Context, lastSavedPath: String)
}