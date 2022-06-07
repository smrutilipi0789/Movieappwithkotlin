package com.smrutilipi.movieeapp

import android.app.Application
import com.smrutilipi.movieeapp.data.network.ApiInterface
import com.smrutilipi.movieeapp.data.network.NetworkConnectionInterceptor
import com.smrutilipi.movieeapp.data.repositories.HomeRepository
import com.smrutilipi.movieeapp.data.repositories.MovieDetailRepository
import com.smrutilipi.movieeapp.ui.home.HomeViewModelFactory
import com.smrutilipi.movieeapp.ui.moviedetail.MovieDetailViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class MovieeApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@MovieeApplication))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { ApiInterface(instance()) }
        bind() from singleton { HomeRepository(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }
        bind() from singleton { MovieDetailRepository(instance()) }
        bind() from provider { MovieDetailViewModelFactory(instance()) }


    }

}