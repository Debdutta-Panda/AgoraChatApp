package com.agorachatapp.charc

import android.util.Log
import com.agorachatapp.Que
import com.agorachatapp.charc.model.ChatPackets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GateCircuit<T> {
    fun newGate(name: String, processor: suspend (List<Result<T>>)->List<Result<T>>): Gate<T>{
        val gate = Gate(name, processor )
        gates[name] = gate
        return gate
    }
    enum class Success{
        TRUE,
        FALSE,
        NONE
    }
    class Result<T>(val success: Success, val data: T)
    class Gate<T>(private val name: String, private val processor: suspend (List<Result<T>>)->List<Result<T>>){
        private var processing: AtomicBoolean = AtomicBoolean(false)
        private var que = mutableListOf<Result<T>>()
        val nextStages: MutableSet<Gate<T>> = mutableSetOf()
        suspend fun put(tag: String, items: List<Result<T>>){
            process(tag, items)
        }
        suspend fun process(tag: String, items: List<Result<T>>) {
            if(items.isEmpty()){
                return
            }
            charLog("$name:$tag")
            if(name=="server"){
                Log.d("que_bug1",que.size.toString())
            }
            que.addAll(items)
            if(name=="server"){
                Log.d("que_bug2",que.size.toString())
            }
            if(processing.get()){
                return
            }
            processing.set(true)
            val results = mutableListOf<Result<T>>()
            val failed = mutableListOf<Result<T>>()
            while(que.isNotEmpty()){
                val pr = mutableListOf<Result<T>>()
                pr.addAll(que)
                que.clear()
                val r = processor(pr)
                results.addAll(r)
                r.forEach {
                    if(it.success == Success.FALSE){
                        failed.add(it)
                    }
                }
                if(name=="server"){
                    Log.d("que_bug_failed",failed.size.toString())
                }
            }
            processing.set(false)
            que.addAll(failed)
            if(name=="server"){
                Log.d("que_bug3",que.size.toString())
            }
            var gateIndex = -1
            if(results.isNotEmpty()){
                nextStages.forEach {
                    it.put("$tag.${++gateIndex}",results)
                }
            }
        }
    }
    var gates: MutableMap<String,Gate<T>> = mutableMapOf()
}