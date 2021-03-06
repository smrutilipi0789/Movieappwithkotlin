package com.smrutilipi.movieeapp.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smrutilipi.movieeapp.R
import com.smrutilipi.movieeapp.databinding.ActivityHomeBinding

import com.smrutilipi.movieeapp.ui.adapter.AdapterMovies
import com.smrutilipi.movieeapp.ui.moviedetail.MovieDetailScrollingActivity

import com.smrutilipi.movieeapp.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class HomeActivity : AppCompatActivity(), KodeinAware {

    companion object {
        const val ANIMATION_DURATION = 1000.toLong()
    }

    override val kodein by kodein()
    private lateinit var dataBind: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private lateinit var adapterMovies: AdapterMovies
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_home)
        setupViewModel()
        setupUI()
        initializeObserver()
        handleNetworkChanges()
        setupAPICall()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.apply {
            queryHint = "Search"
            isSubmitButtonEnabled = true
            onActionViewExpanded()
        }
        search(searchView)
        return true
    }

    private fun setupUI() {
        adapterMovies = AdapterMovies()
        dataBind.recyclerViewMovies.apply {
            layoutManager = GridLayoutManager(context,2)
           // layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = adapterMovies
            addOnItemTouchListener(
                RecyclerItemClickListener(
                    applicationContext,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            if (adapterMovies.getData().isNotEmpty()) {
                                val searchItem = adapterMovies.getData()[position]
                                searchItem?.let {
                                    val intent =
                                        Intent(
                                            applicationContext,
                                            MovieDetailScrollingActivity::class.java
                                        )
                                    intent.putExtra(AppConstant.INTENT_POSTER, it.poster)
                                    intent.putExtra(AppConstant.INTENT_TITLE, it.title)
                                    startActivity(intent)
                                }

                            }
                        }

                    })
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    val visibleItemCount = layoutManager!!.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    viewModel.checkForLoadMoreItems(
                        visibleItemCount,
                        totalItemCount,
                        firstVisibleItemPosition
                    )
                }

            })
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

    }

    private fun initializeObserver() {
        viewModel.movieNameLiveData.observe(this, Observer {
            Log.i("Info", "Movie Name = $it")
        })
        viewModel.loadMoreListLiveData.observe(this, Observer {
            if (it) {
                adapterMovies.setData(null)
                Handler().postDelayed({
                    viewModel.loadMore()
                }, 2000)
            }
        })
    }

    private fun setupAPICall() {
        viewModel.moviesLiveData.observe(this, Observer { state ->
            when (state) {
                is State.Loading -> {
                    dataBind.recyclerViewMovies.hide()
                    dataBind.linearLayoutSearch.hide()
                    dataBind.progressBar.show()
                }
                is State.Success -> {
                    dataBind.recyclerViewMovies.show()
                    dataBind.linearLayoutSearch.hide()
                    dataBind.progressBar.hide()
                    adapterMovies.setData(state.data)
                }
                is State.Error -> {
                    dataBind.progressBar.hide()
                    showToast(state.message)
                }
            }
        })

    }

    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {
                dataBind.textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                dataBind.networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                }
            } else {
                if (viewModel.moviesLiveData.value is State.Error || adapterMovies.itemCount == 0) {
                    viewModel.getMovies()
                }
                dataBind.textViewNetworkStatus.text = getString(R.string.text_connectivity)
                dataBind.networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        })
    }

    private fun search(searchView: SearchView) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                dismissKeyboard(searchView)
                searchView.clearFocus()
                viewModel.searchMovie(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }


}