package com.agorachatapp

import java.util.*

class Que<T>(var callback: (Que<T>,List<T>)->Unit) {
    var running = false
    private set
    private val list = mutableListOf<T>()
    private val tempList = mutableListOf<T>()
    fun put(item: T){
        if(running){
            tempList.add(item)
        }
        else{
            list.add(item)
        }
        run()
    }
    fun run(){
        if(running){
            return
        }
        running = true
        callback(this,list)
        running = false
        if(tempList.isNotEmpty()){
            list.addAll(tempList)
            tempList.clear()
            run()
        }
    }

    fun remove(toRemove: List<T>) {
        list.removeAll(toRemove)
    }
}