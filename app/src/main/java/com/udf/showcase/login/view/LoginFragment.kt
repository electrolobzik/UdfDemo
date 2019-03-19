package com.udf.showcase.login.view

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import com.badoo.mvicore.android.AndroidBindings
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import com.badoo.mvicore.binder.Binder
import com.udf.showcase.BaseFragment
import com.udf.showcase.R
import com.udf.showcase.login.di.LoginModule
import com.udf.showcase.login.presenter.LoginPresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import javax.inject.Inject

class LoginFragment : BaseFragment(), ILoginView {

    @Inject lateinit var presenter: LoginPresenter

    @BindView(R.id.login_til) lateinit var loginInput: TextInputLayout
    @BindView(R.id.login) lateinit var loginText: TextInputEditText
    @BindView(R.id.password_til) lateinit var passwordInput: TextInputLayout
    @BindView(R.id.password) lateinit var passwordText: TextInputEditText
    @BindView(R.id.login_btn) lateinit var loginBtn: Button
    @BindView(R.id.error) lateinit var errorTxt: TextView
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.save_credentials_cb) lateinit var saveCredentialsCb: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getActivityComponent()
            .plusLoginComponent(LoginModule(this))
            .inject(this)
    }

    override fun getLayoutRes(): Int = R.layout.login_layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDisposables.add(presenter.addLoginInput(RxTextView.textChanges(loginText)))
        viewDisposables.add(presenter.addPasswordInput(RxTextView.textChanges(passwordText)))
        loginBtn.setOnClickListener { presenter.loginBtnClick() }
        saveCredentialsCb.setOnCheckedChangeListener { buttonView, isChecked ->
            hideKeyboard()
            presenter.onSaveCredentialsCheck(isChecked)
        }

        Binder(CreateDestroyBinderLifecycle(lifecycle)).apply {
            bind(view to feature using UiEventToWish)
            bind(feature to view using StateToViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun setProgress(show: Boolean) {
        loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPasswordError(errorText: String?) {
        errorText?.let {
            passwordInput.error = errorText
        } ?: run {
            passwordInput.error = ""
        }
    }

    override fun showLoginError(errorText: String?) {
        errorText?.let {
            loginInput.error = errorText
        } ?: run {
            loginInput.error = ""
        }
    }

    override fun setError(error: String?) {
        error?.let {
            errorTxt.visibility = View.VISIBLE
            errorTxt.text = error
        } ?: run {
            errorTxt.visibility = View.GONE
        }
    }

    override fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        imm?.hideSoftInputFromWindow(loginText.windowToken, 0)
    }

    override fun setEnableLoginBtn(enabled: Boolean) {
        loginBtn.isEnabled = enabled
    }


}
