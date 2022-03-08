package com.agorachatapp.charc

import android.util.Log
import com.agorachatapp.Que
import com.agorachatapp.charc.model.ChatPackets

class GateCircuit<T> {
    enum class Success{
        TRUE,
        FALSE,
        NONE
    }
    class Result<T>(val success: Success, val data: T)
    class Gate<T>(private val name: String, private val processor: suspend (List<Result<T>>)->List<Result<T>>){
        private var processing = false
        private var que = mutableListOf<Result<T>>()
        val nextStages: MutableSet<Gate<T>> = mutableSetOf()
        suspend fun put(items: List<Result<T>>){
            process(items)
        }
        suspend fun process(items: List<Result<T>>) {
            if(items.isEmpty()){
                return
            }
            que.addAll(items)
            if(processing){
                return
            }
            processing = true
            val results = mutableListOf<Result<T>>()
            val failed = mutableListOf<Result<T>>()
            while(que.isNotEmpty()){
                val pr = mutableListOf<Result<T>>()
                pr.addAll(que)
                que.clear()
                val r = processor(items)
                results.addAll(r)
                r.forEach {
                    if(it.success == Success.FALSE){
                        failed.add(it)
                    }
                }
            }
            processing = false
            que.addAll(failed)
            nextStages.forEach {
                it.put(results)
            }
        }
    }
    var gates: MutableMap<String,Gate<ChatPackets>> = mutableMapOf()
}