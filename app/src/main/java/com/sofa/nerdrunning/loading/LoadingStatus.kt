package com.sofa.nerdrunning.loading

sealed class LoadingStatus<T>

class Loading<T> : LoadingStatus<T>()
class Loaded<T>(val value: T) : LoadingStatus<T>()
