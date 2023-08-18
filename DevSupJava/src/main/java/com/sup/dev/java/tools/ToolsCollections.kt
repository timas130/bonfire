package com.sup.dev.java.tools

object ToolsCollections {

    fun <K> sort(list:ArrayList<K>, comparator:(K, K)->Number){
        list.sortWith(Comparator { o1, o2 ->
            val x = comparator.invoke(o1, o2)
            when {
                (x.toLong() == 0L && x.toDouble() == 0.0) -> 0
                (x.toLong() > 0) -> 1
                (x.toDouble() > 0) -> 1
                else -> -1
            }
        })
    }

    inline fun <reified K> merge(vararg arrays: Array<K>):Array<K> {
        val list = ArrayList<K>()
        for(i in arrays) for(n in i) list.add(n)
        return list.toTypedArray()
    }


    fun subarray(array: ByteArray, start:Int, size:Int):ByteArray {
        return ByteArray(size){array[it + start]}
    }

    inline fun <reified K> subarray(array: Array<K>, start:Int, size:Int):Array<K> {
        return Array(size){array[it + start]}
    }

    fun  <K> indexOf(list: ArrayList<K>, comparator:(K)->Boolean):Int{
        for(i in list.indices) if(comparator.invoke(list[i])) return i
        return -1
    }

    inline fun <reified K> removeIf(list: Array<K>, comparator:(K)->Boolean):Array<K> {

        var listV = list

        var i = 0
        while (i < listV.size) {
            val c = listV[i]
            if (comparator.invoke(c))
                listV = remove(i--, listV)
            i++
        }

        return listV
    }

    fun <K> removeIf(list: ArrayList<K>, comparator:(K)->Boolean) {

        var i = 0
        while (i < list.size) {
            val c = list[i]
            if (comparator.invoke(c))
                list.removeAt(i--)
            i++
        }
    }

    fun combine(list:List<ByteArray>) =  combine(*(list.toTypedArray()))

    fun combine(vararg list:ByteArray):ByteArray{
        var size = 0
        for(b in list) size += b.size

        var postion = 0
        val array = ByteArray(size)
        for(b in list) for(bt in b) array[postion++] = bt

        return array
    }

    fun cut(array: ByteArray, offset: Int, size: Int): ByteArray {
        val newArray = ByteArray(size)
        for (i in offset until newArray.size) newArray[i - offset] = array[i]
        return newArray
    }

    inline fun <K> random(array: ArrayList<K>): K {
        return array[ToolsMath.randomInt(0, array.size - 1)]
    }

    inline fun <reified K> random(array: Array<K>): K {
        return array[ToolsMath.randomInt(0, array.size - 1)]
    }

    inline fun <reified K> expand(array: Array<K>, size: Int, filler: K): Array<K> {
        return Array(size) {
            if (array.size > it) array[it]
            else filler
        }
    }

    inline fun <reified K> copy(array: Array<K>): Array<K> {
        return Array(array.size) { array[it] }
    }

    inline fun <reified K> copy(array: ArrayList<K>): ArrayList<K> {
        val list = ArrayList<K>()
        for(i in array) list.add(i)
        return list
    }


    inline fun <reified K> remove(index: Int, array: Array<K>): Array<K> {
        var x = 0
        return Array(array.size - 1) {
            if (it == index) x++
            array[it + x]
        }
    }

    inline fun <reified K> removeItem(item: K, array: Array<K>): Array<K> {

        var count = 0
        for (i in array) if (i == item) count++

        var x = 0
        return Array(array.size - count) {
            if (array[it] == item) x++
            array[it + x]
        }
    }

    inline fun <reified K> add(value: K, array: Array<K>): Array<K> {
        return Array(array.size + 1) {
            if (it == array.size) value
            else array[it]
        }
    }

}