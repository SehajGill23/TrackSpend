package com.example.trackspend.data.model

data class StoreCount(
    val store: String?,   // nullable because some entries may be null
    val count: Int
)