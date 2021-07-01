package com.example.aicb

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.annotation.WorkerThread
import kr.co.shineware.nlp.komoran.core.Komoran
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import org.tensorflow.lite.Interpreter

class Classification(_context: Context) {
    private val TAG = "ClassificationDemo"
    private val MODEL_PATH = "model.tflite"
    private val DIC_PATH = "android_dict.txt"
    private val LABEL_PATH = "labels.txt"
    private val SENTENCE_LEN = 15
    private val MAX_RESULTS = 217
    private val UNKNOWN = "OOV"

    private var context = _context
    private lateinit var dic: MutableMap<String, Int>
    private lateinit var labels: MutableList<String>
    private lateinit var tflite:Interpreter

    class Result(val _id: String, val _title: String, val _confidence: Float){
        private var id = _id
        private var title = _title
        private var confidence = _confidence

        fun getId(): String? {
            return id
        }

        fun getTitle(): String? {
            return title
        }

        fun getConfidence(): Float? {
            return confidence
        }
        override fun toString(): String {
            return "Result(_id='$_id', _title='$_title', _confidence=$_confidence, id='$id', title='$title', confidence=$confidence)"
        }
    }

    /* load model */
    @WorkerThread//한번 실행할 경우 영원히 실행
    fun load(){
        //Syncronized를 이용하여 하나씩 실행
        loadModel()
        loadDictionary()
        loadLabels()
    }

    @WorkerThread
    @Synchronized
    private fun loadModel(){
        try{
            var buffer = loadModelFile(this.context.getAssets())
            tflite = Interpreter(buffer)
            Log.d(TAG, "TFLite model loaded")
        }catch (e: IOException){
            Log.e(TAG, e.message!!)
        }
    }

    /* Load words dictionary. */
    @WorkerThread
    @Synchronized
    private fun loadDictionary(){
        try {
            loadDictionaryFile(context.assets)
            Log.v(TAG, "Dictionary loaded")
        } catch (ex: IOException) {
            Log.e(TAG, ex.message!!)
        }
    }

    /* Load labels.  */
    @WorkerThread
    @Synchronized
    private fun loadLabels() {
        try {
            loadLabelFile(context.assets)
            Log.v(TAG, "Labels loaded")
        } catch (ex: IOException) {
            Log.e(TAG, ex.message!!)
        }
    }

    @WorkerThread
    @Synchronized
    fun unload() {
        tflite.close()
        dic.clear()
        labels.clear()
    }

    @WorkerThread
    @Synchronized
    fun classify(komoran: Komoran, text: String): List<Result>
    {
        //전처리
        var pos_text = posTag(komoran, text)

        var input = tokenizeInputText(pos_text)
        Log.d("classify", "" + pos_text)

        var output = Array(1) { FloatArray(labels.size) }
        tflite.run(input, output)

        // best 분류 결과 찾기
        val pq: PriorityQueue<Result> =
            PriorityQueue<Result>(
                MAX_RESULTS,
                Comparator<Result> { lhs: Result, rhs: Result ->
                    java.lang.Float.compare(
                        rhs.getConfidence()!!,
                        lhs.getConfidence()!!
                    )
                })
        for (i in labels.indices) {
            pq.add(Result("" + i, labels[i], output[0][i]))
        }
        val results: ArrayList<Result> =
            ArrayList<Result>()
        while (!pq.isEmpty()) {
            results.add(pq.poll())
        }

        return results
    }

    /* Load Tf Lite Model from assets directory */
    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager): MappedByteBuffer{
        assetManager.openFd(MODEL_PATH).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    startOffset,
                    declaredLength
                )
            }
        }
    }

    /** Load dictionary from assets.  */
    @Throws(IOException::class)
    private fun loadLabelFile(assetManager: AssetManager) {
        var first = 0
        assetManager.open(LABEL_PATH).use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                // Each line in the label file is a label.
                while (reader.ready()) {
                    val s = reader.readLine()
                    if (first != 0) labels.add(s)
                    first = 1
                }
            }
        }
    }

    /** Load labels from assets.  */
    @Throws(IOException::class)
    private fun loadDictionaryFile(assetManager: AssetManager) {
        assetManager.open(DIC_PATH).use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                // dic의 컬럼1 단어, 컬럼1 인덱스
                while (reader.ready()) {
                    val line = Arrays.asList(*reader.readLine().split(" ").toTypedArray())
                    if (line.size < 2) {
                        continue
                    }
                    dic[line[0]] = line[1].toInt()
                }
            }
        }
    }

    /** Pre-prosessing: tokenize and map the input words into a float array.  */
    fun tokenizeInputText(text: String): Array<FloatArray> {
        val tmp = FloatArray(SENTENCE_LEN)
        val array = Arrays.asList(*text.split(" ").toTypedArray())
        var index = 0
        for (word in array) {
            if (index >= SENTENCE_LEN) {
                break
            }
            if(dic.containsKey(word))
                tmp[SENTENCE_LEN - array.size + index++] = dic.get(word)!!.toFloat()
            else
                tmp[SENTENCE_LEN - array.size + index++] = 1.0F

            Log.d(
                "tokenizeInputText", "" + tmp[SENTENCE_LEN - array.size + index - 1]
            )
        }
        // Padding and wrapping.
        // Arrays.fill(tmp, index, SENTENCE_LEN - 1, (int) dic.get(UNKNOWN));
        return arrayOf(tmp)
    }

    @Synchronized
    fun posTag(komoran: Komoran, text: String): String {
        val analyzeResultList = komoran.analyze(text)
        val tokenList = analyzeResultList.tokenList
        var save = ""
        for (token in tokenList) {
            if (token.pos != "JKS" && token.pos != "JKC" && token.pos != "JKG" &&
                token.pos != "JKO" && token.pos != "JKB" && token.pos != "JKV" &&
                token.pos != "JKQ" && token.pos != "JX" && token.pos != "JC" &&
                token.pos != "SF" && token.pos != "SP" && token.pos != "SS" &&
                token.pos != "SE" && token.pos != "SO" && token.pos != "EP" &&
                token.pos != "EF" && token.pos != "EC" && token.pos != "ETN" &&
                token.pos != "ETM" && token.pos != "XSN" && token.pos != "XSV" &&
                token.pos != "XSA"
            ) save += token.morph + " "
        }
        return save
    }
}