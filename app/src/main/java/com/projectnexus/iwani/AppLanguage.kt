package com.projectnexus.iwani

enum class AppLanguage {
    ENGLISH,
    TAGALOG;

    val isTagalog: Boolean get() = this == TAGALOG
}
