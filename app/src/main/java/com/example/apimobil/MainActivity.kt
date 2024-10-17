package com.example.apimobil

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

class MainActivity : AppCompatActivity() {
    lateinit var githubService: GithubService
    lateinit var result: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val vvod = findViewById<EditText>(R.id.vvod)
        val click = findViewById<Button>(R.id.button)
        result = findViewById<TextView>(R.id.res)

        click.setOnClickListener {
            val username = vvod.text.toString()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            githubService = retrofit.create(GithubService::class.java)

            getRepositories(username)
        }
    }

    private fun getRepositories(username: String) {
        val token = getString(R.string.api_key)
        githubService.getRepositories(username, token)
            .enqueue(object : Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    if (response.isSuccessful) {
                        val repositories = response.body()
                        displayRepositories(repositories)
                    } else {
                        Log.e("Github API", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Log.e("Github API", "Network error: ${t.message}")
                }
            })
    }

    private fun displayRepositories(repositories: List<Repository>?) {
        if (repositories != null) {
            val repositoryInfo = StringBuilder()
            for (repository in repositories) {
                repositoryInfo.append("Название: ${repository.name}\n")
                repositoryInfo.append("Описание: ${repository.description}\n")
            }
            result.text = repositoryInfo.toString()
        } else {
            result.text = "Ошибка получения данных"
        }
    }

    // Модель репозитория
    data class Repository(
        val name: String,
        val description: String?
    )

    interface GithubService {
        @GET("users/{username}/repos")
        fun getRepositories(
            @Path("username") username: String,
            @Header("Authorization") token: String
        ): Call<List<Repository>>
    }
}
