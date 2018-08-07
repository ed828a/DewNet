package com.example.edward.dewnet.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.example.edward.dewnet.R
import com.example.edward.dewnet.R.id.mainListView
import com.example.edward.dewnet.R.id.swipeRefreshLayout
import com.example.edward.dewnet.adapter.MainListAdapter
import com.example.edward.dewnet.model.NetworkState
import com.example.edward.dewnet.model.VideoModel
import com.example.edward.dewnet.util.DEFAULT_QUERY
import com.example.edward.dewnet.util.KEY_QUERY
import com.example.edward.dewnet.util.VIDEO_MODEL
import com.example.edward.dewnet.viewmodel.MainViewModel
import com.example.edward.dewnet.viewmodel.MainViewModelFactory

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var viewModelFactory: MainViewModelFactory
    private val videoViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private lateinit var preferences: SharedPreferences
    private lateinit var query: String
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        DewNetApp.appComponent.inject(this)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        preferences = DewNetApp.sharedPreferences
        query = preferences.getString(KEY_QUERY, DEFAULT_QUERY)

        initActionBar()
        initRecyclerView()
        initSwipeToRefresh()

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(KEY_QUERY)
        }

        videoViewModel.showSearchQuery(query)
    }



    override fun onResume() {
        super.onResume()
        hideKeyboard()
        searchView?.clearFocus()
    }

    private fun initActionBar() {
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
    private fun initRecyclerView() {
        val adapter = MainListAdapter(
                {
                    val intent = Intent(this@MainActivity, ExoVideoPlayActivity::class.java)
                    intent.putExtra(VIDEO_MODEL, it)
                    startActivity(intent)
                },
                { videoViewModel.retry() }
        )

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainListView.layoutManager = GridLayoutManager(this, 2)
        } else {
            mainListView.layoutManager = LinearLayoutManager(this)
        }

        mainListView.adapter = adapter
        mainListView.setHasFixedSize(true)
        videoViewModel.videoList.observe(this, Observer<PagedList<VideoModel>> {
            adapter.submitList(it)
        })
        videoViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        videoViewModel.refreshState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = (it == NetworkState.LOADING)
        })
        swipeRefreshLayout.setOnRefreshListener {
            videoViewModel.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        searchView = menu.findItem(R.id.menu_search).actionView as SearchView?

        val view: SearchView = searchView!!
        if (searchView != null) initSearchView(view)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        searchView.layoutParams = ActionBar.LayoutParams(Gravity.END)
        searchView.queryHint = "Search Movie ..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()?.let {
                    if (it.isNotEmpty() && videoViewModel.showSearchQuery(it)) {
                        mainListView.scrollToPosition(0)
                        (mainListView.adapter as? MainListAdapter)?.submitList(null)
                        preferences.edit().putString(KEY_QUERY, query).apply()
                    }
                }

                hideKeyboard()
                searchView.clearFocus()
                searchView.setQuery("", false)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(KEY_QUERY, videoViewModel.currentQuery())
    }

}
