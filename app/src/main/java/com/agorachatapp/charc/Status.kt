package com.agorachatapp.charc

class Status {
    private var values = mutableListOf<Int>()
    companion object{
        const val zero = '0'
        fun decode(input: String): Status{
            val r = Status()
            if(input.isNotEmpty()){
                for(c in input){
                    r.values.add(c-zero)
                }
            }
            return r
        }
    }
    operator fun get(index: Int): Int{
        return values.getOrElse(index) {-1}
    }
    operator fun set(index: Int, value: Int){
        if(index<0){
            return
        }
        if(index < values.size){
            values[index] = value
        }
        else{
            val s = values.size
            for(i in s..index){
                values.add(0)
            }
            values[index] = value
        }
    }
    fun encode(): String{
        return String(
            values.map {
                Char(it+zero.code)
            }.toCharArray()
        )
    }

    fun forEachIndexed(block: (Int,Int)->Unit){
        values.forEachIndexed(block)
    }

    fun upgrade(new: Status): Status {
        val cloned = values.map {
            it
        }.toMutableList()
        new.forEachIndexed{ index,item->
            if(item>this[index]){
                cloned[index] = item
            }
        }
        val r = Status()
        r.values = cloned.toMutableList()
        return r
    }
}