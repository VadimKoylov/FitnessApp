package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.JsonReader
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        button_sign_in.isEnabled = false
        button_sign_in.isClickable = false

        val password: EditText = findViewById(R.id.some_id)
        val showPassword: CheckBox = findViewById(R.id.show_password)

        showPassword.setOnCheckedChangeListener { _, b ->
            if (b) {
                password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                password.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        nickname.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

                val nickname = edit_nickname.text.toString()
                val password = password.text.toString()

                if (nickname.isNotEmpty() && password.isNotEmpty() && password.length >= 3) {
                    button_sign_in.isEnabled = true
                    button_sign_in.isClickable = true
                } else {
                    button_sign_in.isEnabled = false
                    button_sign_in.isClickable = false
                }
            }
        })

        password.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val nickname = edit_nickname.text.toString()
                val password = password.text.toString()
                if (nickname.isNotEmpty() && password.isNotEmpty() && password.length >= 3) {
                    button_sign_in.isEnabled = true
                    button_sign_in.isClickable = true
                } else {
                    button_sign_in.isEnabled = false
                    button_sign_in.isClickable = false
                }
            }
        })

        button_sign_in.setOnClickListener {

            val job: Job = GlobalScope.launch(Dispatchers.IO) {
                var token: String
                try {

                    val url = "https://app.ferfit.club/api/auth/refresh-tokens"
                    val endpoint = URL(url)

                    val myConnection: HttpsURLConnection =
                        endpoint.openConnection() as HttpsURLConnection

                    val authorizationKey =
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NTksImlhdCI6MTYyMDQ5MTYxNCwiZXhwIjoxMDAxNjIwNDkxNjE0fQ.zGqmT0dH2bUMkG5DltUciML5CCXDbXsdO3p5a6AH5Z8"
                    val contentType = "application/json"
                    myConnection.setRequestProperty("Authorization", "Bearer $authorizationKey")
                    myConnection.setRequestProperty("Content-Type", contentType)
                    myConnection.requestMethod = "POST";

                    if (myConnection.responseCode == 200) {

                        val responseBody: InputStream = myConnection.inputStream
                        val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
                        val jsonReader = JsonReader(responseBodyReader)

                        jsonReader.beginObject()

                        while (jsonReader.hasNext()) {
                            val key = jsonReader.nextName()
                            if (key == "result") {

                                jsonReader.beginObject();
                                while (jsonReader.hasNext()) {
                                    val arrayKey = jsonReader.nextName();
                                    if (arrayKey == "access") {
                                        token = jsonReader.nextString();
                                        myConnection.disconnect();

                                        val intent = Intent(this@SignUp, News::class.java)
                                        intent.putExtra("token", token);
                                        startActivity(intent)
                                        finish()
                                        break;
                                    }
                                }
                                break;
                            } else {
                                jsonReader.skipValue()
                            }
                        }
                        jsonReader.close();
                    } else {
                        token = myConnection.responseCode.toString();
                    }
                    myConnection.disconnect();

                } catch (e: Exception) {
                    token = e.toString()
                    e.printStackTrace()
                }
            }
        }
    }
}