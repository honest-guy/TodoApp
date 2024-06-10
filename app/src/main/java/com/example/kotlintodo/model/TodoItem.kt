package com.example.kotlintodo.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class TodoItem():RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var todo: String = ""
    var completed: Boolean = false
    var userId: Long = 0
}


