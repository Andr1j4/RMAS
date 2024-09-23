package com.example.aplikacija.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginVM(private val userRep: UserRepository?) : ViewModel() {


    private val _emailLogin = MutableStateFlow("")
    val emailLogin: StateFlow<String> = _emailLogin.asStateFlow()
    fun updateEmailTp(nT: String) {
        _emailLogin.value = nT
        okToLogin()
    }

    private val _passwordLogin = MutableStateFlow("")
    val passwordLogin: StateFlow<String> = _passwordLogin.asStateFlow()
    fun updatePasswordTp(nT: String) {
        _passwordLogin.value = nT
        okToLogin()
    }

    private val _showPasswordLogin = MutableStateFlow(false)
    val showPasswordLogin: StateFlow<Boolean> = _showPasswordLogin.asStateFlow()
    fun updateShowPasswordLogin(bl: Boolean) {
        _showPasswordLogin.value = bl
    }


    private val _firebaseOk = MutableStateFlow<Boolean>(false)
    val firebaseOk: StateFlow<Boolean> = _firebaseOk.asStateFlow()


    private val _loginAttempted = MutableStateFlow(false)
    val loginAttempted: StateFlow<Boolean> = _loginAttempted.asStateFlow()
    fun resetLoginAttempt() {
        _loginAttempted.value = false
    }


    fun login() {
        viewModelScope.launch {
            try {
                val usr = userRep?.login(_emailLogin.value, _passwordLogin.value)
                _firebaseOk.value = (usr?.uid != null)
                Log.w("FIREOK", _firebaseOk.value.toString())
            } catch (e: Exception) {
                _firebaseOk.value = false
            } finally {
                _loginAttempted.value = true
            }
        }
    }

    private val _okToLogin = MutableStateFlow<Boolean>(false)
    val okToLogin: StateFlow<Boolean> = _okToLogin.asStateFlow()
    fun okToLogin() {
        if (_emailLogin.value.isNotEmpty()
            && _passwordLogin.value.isNotEmpty()
        ) {
            _okToLogin.value = true

        } else {
            _okToLogin.value = false
        }

    }

}

class LoginVMFactory(private val userRep: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginVM::class.java)) {
            return LoginVM(userRep) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}