package com.example.aicb

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.database.Cursor
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.aicb.DB.ChatTable
import com.example.aicb.SplashActivity.Companion.model
import com.pedro.library.AutoPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var classification: Classification
    private lateinit var reply: Array<Array<String?>>
    private var reply_length = 0
    private lateinit var answer: Array<String?>
    private lateinit var map: Fragment
    private lateinit var adapter: ChatAdapter
    private lateinit var cursor: Cursor
    private lateinit var chatTable: ChatTable
    private lateinit var input: String
    private lateinit var output: String

    companion object {
        var hospital: ArrayList<hospital_info>? = null
    }


    override fun onStart() {
        super.onStart()
        Log.d("onStart", "running successfully")

        //사전 준비
        classification.load()

        //답변 준비
        //komoran = new Komoran(DEFAULT_MODEL.FULL);

        //답변 준비
        reply = Array<Array<String?>>(600) { arrayOfNulls(2) }
        try {
            load_reply(applicationContext.assets)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("reply", "replay failed")
        }

        answer = arrayOfNulls<String>(10)

        map = MapFragment()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AutoPermissions.loadAllPermissions(this,101)
        }

    }

    override fun onStop() {
        super.onStop()
        classification.unload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        classification = Classification(applicationContext)
        adapter = ChatAdapter()
        chatTable = ChatTable()
        chatTable.openDatabase(applicationContext, "Chat_log")
        chatTable.createTable("Chat_log")
        cursor = chatTable.selectData("Chat_log")!!

        if (cursor.count != 0) {
            for (i in 0 until cursor.count) {
                cursor.moveToNext()
                Log.d("Chat_log", cursor.getString(0) + " " + cursor.getInt(1) + " " + i)
                adapter.addItem(ChatItem(cursor.getString(0), cursor.getInt(1), cursor.getInt(2)))
            }
            listView.setAdapter(adapter)
        }
        scrollView.post(Runnable {
            kotlin.run {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        })


        button.setOnClickListener {
            val contents = editText.text.toString()
            Log.d("click", contents)
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }

            if (!contents.equals("")) {
                chatTable.insert("Chat_log", contents, 0, 0)
                classify(contents)
            }
        }

        //인터넷 접속, 전화걸기
        listView.setOnItemClickListener(
            AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
                fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val chatItem = adapter.getItem(position) as ChatItem
                    Log.d("action", "" + chatItem.action)

                    //action이 1이라면 홈페이지 접속, 2라면 전화걸기
                    if (chatItem.action == 1) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(chatItem.chat))
                        startActivity(intent)
                    } else if (chatItem.action == 2) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + chatItem.chat))
                        startActivity(intent)
                    }
                }
            })



     /*//정신건강관련전체기관정보에서 병원 이름과 주소를 받아오고, 주소를 위도와 경도로 변환
        //https://www.data.go.kr/data/3049990/fileData.do
        //시간이 많이 소요되므로 좌표를 미리 변환 후, 텍스트 파일로 저장.
        try {
            GetHospitalPoints();
        } catch (e: IOException) {
            e.printStackTrace();
            Log.d("GetHospitalPoints", "오류 발생");
        }*/

        //위 동작을 마무리 했다면 변환된 좌표를 텍스트 파일을 하나 생성하여 assets 폴더안에 저장한다. 그 후 앱을 다시 실행
        //hostpital.txt 저장 형태는 병원 이름, 위도, 경도, 인터넷 주소, 전화번호
        try {
            AddressToGPScoordinates()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun load_reply(assetManager: AssetManager) {
        reply_length = 0
        Log.d("load_reply", "loading replying.txt")

        assetManager.open("reply.txt").use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                while (reader.ready()) {
                    val line = reader.readLine().split("\t").toTypedArray()
                    reply[reply_length][0] = line[0]
                    reply[reply_length][1] = line[1]
                    reply_length++
                }
            }
        }
    }

    fun classify(text: String?) {
        val results: List<Classification.Result> = classification.classify(
            model.getKomoran(),
            text!!
        )
        val pos_text = classification.posTag(model.getKomoran(), text)

        //float[][] input_embed = classification.tokenizeInputText(pos_text);
        var result = ""
        result = results[0].getTitle().toString()
        //result = results.get(index).getTitle() + " " + "confidence: " + results.get(index).getConfidence();

        var check = false
        var count = 0
        //의도에 맞는 reply 데이터를 저장한 뒤, 랜덤으로 골라 답변으로 출력
        for (i in 0 until reply_length) {
            if (reply[i][0] == result) {
                answer[count] = reply[i][1]
                count++
                check = true
            }
            else if (check && reply[i][0] != result) {
                break
            }
        }
        val rand = Random()
        val ran: Int
        if (count != 0) {
            ran = rand.nextInt(count)
            output = answer[ran].toString()
        }
        else {
            output = "미안해요, 무슨말인지 잘 모르겠어요."
        }
        input = result

        Log.d("output:", output)
        adapter.addItem(ChatItem(text, 0, 0))
        Log.d("input:", text)
        listView.adapter = adapter

        val handler = Handler()
        handler.postDelayed({
            //끝에 1이 있는 reply일 경우, 병원 안내 서비스 제공
            //일반 chat일 경우 action이 0, 1은 인터넷 주소, 2는 전화번호
            if (output.get(output.length - 1) != '1') {
                adapter.addItem(ChatItem(output, 1, 0))
                chatTable.insert("Chat_log", output, 1, 0)
            } else {
                adapter.addItem(ChatItem(output.substring(0, output.length - 2), 1, 0))
                chatTable.insert("Chat_log", output.substring(0, output.length - 2), 1, 0)
                var check = 0
                while (check == 0) {
                    check = startLocationService()
                }
                supportFragmentManager.beginTransaction().replace(R.id.container, map).commit()
                handler.postDelayed({
                    //가까운 병원 주소 및 전화번호 소개
                    Log.d("shortest_address", model.getShortest_address().value!!)
                    adapter.addItem(
                        ChatItem(
                            "가까운 " + model.getShortest_name().value!! + "에서 상담을 받아봐요",
                            1,
                            0
                        )
                    )
                    chatTable.insert(
                        "Chat_log",
                        "가까운 " + model.getShortest_name().value!! + "에서 상담을 받아봐요",
                        1,
                        0
                    )

                    //가끔 인터넷 주소 정보가 없는 경우도 있음
                    if (!model.getShortest_address().value.equals("https://")) {
                        adapter.addItem(ChatItem(model.getShortest_address().value!!, 1, 1))
                        chatTable.insert("Chat_log", model.getShortest_address().value!!, 1, 1)
                    }

                    adapter.addItem(ChatItem(model.getShortest_number().value!!, 1, 2))
                    chatTable.insert("Chat_log", model.getShortest_number().value!!, 1, 2)
                    listView.adapter = adapter
                }, 5000)
            }
            listView.adapter = adapter
        }, 1000)
        editText.text.clear()
        // Show classification result on screen
    }

    fun startLocationService(): Int //내 좌표 검색
    {
        val manager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            val gpsListener = GPSListener()
            var location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,gpsListener);
            while (location == null) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1f, gpsListener)
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
        } catch (e: SecurityException) {
            Toast.makeText(applicationContext, "권한 부여 필요", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return 1
    }

    inner class GPSListener : LocationListener {

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            val message = "latitude: $latitude Longitude: $longitude"
            Log.d("현재 위치", message)
        }

        private fun distance(
            my_latitude: Double,
            my_longitude: Double,
            h_latitude: Double,
            h_longitude: Double
        ): Double {
            val theta = my_longitude - h_longitude
            var distance = Math.sin(deg2rad(my_latitude)) * Math.sin(deg2rad(h_latitude)) +
                    Math.cos(deg2rad(my_latitude)) * Math.cos(deg2rad(h_latitude)) * Math.cos(
                deg2rad(theta)
            )
            distance = Math.acos(distance)
            distance = rad2deg(distance)
            distance = distance * 60 * 1.1515
            return distance
        }

        // This function converts decimal degrees to radians
        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        // This function converts radians to decimal degrees
        private fun rad2deg(rad: Double): Double {
            return rad * 180 / Math.PI
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    //병원 정보는 이름, 위도, 경도, 인터넷 주소, 전화번호
    class hospital_info(
        var name: String,
        var latitdue: Double,
        var longitude: Double,
        var internet_address: String,
        var number: String
    )

    inner class ChatAdapter : BaseAdapter() {
        var item: ArrayList<ChatItem> = ArrayList<ChatItem>()
        override fun getCount(): Int {
            return item.size
        }

        override fun getItem(position: Int): Any {
            return item[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view:ChatItemView
            if(convertView == null)
            {
                view = ChatItemView(applicationContext)
            }
            else
            {
                view = convertView as ChatItemView
            }

            val items:ChatItem = item.get(position)

            view.setChat(items.chat, items.type, applicationContext)

            return view
        }

        fun addItem(items: ChatItem) {
            item.add(items)
        }
    }

    @Throws(IOException::class)
    fun GetHospitalPoints() {
        val assetManager = applicationContext.assets
        assetManager.open("정신건강관련전체기관정보_2019.txt").use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                val address: MutableList<Pair<String, Pair<String, Pair<String, String>>>> = ArrayList()
                while (reader.ready()) {
                    val line = Arrays.asList(*reader.readLine().split(",").toTypedArray())
                    if (line.size < 8)
                        continue
                    address.add(Pair(line[3], Pair(line[7], Pair(line[6], line[5]))))
                }
                SearchHospitalPoints(address)
            }
        }
    }

    //한글 주소를 경도와 위도로 변환, Log.d에 한글 주소, 위도, 경도, 인터넷 주소, 전화번호로 출력하기
    @Throws(IOException::class)
    fun SearchHospitalPoints(info: List<Pair<String, Pair<String, Pair<String, String>>>>) {
        val geocoder = Geocoder(this)
        var address: List<Address>? = null
        val size = info.size
        val file = File(filesDir, "hospital.txt")
        val writer = BufferedWriter(FileWriter(file))
        for (i in 1 until size) {
            try {
                address = geocoder.getFromLocationName(info[i].second.first, 3)
                if (address.size == 0) //주소를 못찾았다면 넘어감
                    continue
                else {
                    //찾았다면 log를 찍기, 이 log정보를 복사하여 hospital.txt 파일을 assets폴더에 저장
                    writer.write(info[i].toString() + " " + address[0].latitude + " " + address[0].longitude)
                    writer.newLine()
                    Log.d(
                        "이름/좌표",
                        info[i].first.toString() + " " + address[0].latitude + " " + address[0].longitude + " " + info[i].second.second.first + " " + info[i].second.second.second
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(info[i].first.toString() + "error", "" + info[i].second)
            }
        }
        writer.flush()
    }

    @Throws(IOException::class)
    fun AddressToGPScoordinates() {
        val assetManager = applicationContext.assets
        hospital = ArrayList()
        var name: String
        var i = 0
        var left: Int
        assetManager.open("hospital.txt").use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                while (reader.ready()) {
                    name = ""
                    i = 1
                    val line = Arrays.asList(*reader.readLine().split(" ").toTypedArray())
                    while (true) {
                        name +=
                            try {
                                line[i].toDouble()
                                break
                            } catch (e: Exception) {
                                line[i].toString() + " "
                            }
                        i++
                    }
                    left = line.size - i
                    if (left == 4)
                        hospital!!.add(
                            hospital_info(
                                name,
                                line[i].toDouble(),
                                line[i + 1].toDouble(),
                                line[i + 2],
                                line[i + 3]
                            )
                        ) else  //주소 정보가 없을 경우 ""을 삽입
                        hospital!!.add(
                            hospital_info(
                                name,
                                line[i].toDouble(),
                                line[i + 1].toDouble(),
                                "",
                                line[i + 3]
                            )
                        )
                }
            }
        }
    }
}