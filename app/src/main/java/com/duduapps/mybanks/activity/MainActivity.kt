package com.duduapps.mybanks.activity

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.adapter.AccountsAdapter
import com.duduapps.mybanks.databinding.ActivityMainBinding
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.util.API_ACCOUNT
import com.duduapps.mybanks.util.API_AGENCY
import com.duduapps.mybanks.util.API_BANK_ID
import com.duduapps.mybanks.util.API_DELETED
import com.duduapps.mybanks.util.API_DOCUMENT
import com.duduapps.mybanks.util.API_HOLDER
import com.duduapps.mybanks.util.API_ID
import com.duduapps.mybanks.util.API_LABEL
import com.duduapps.mybanks.util.API_LEGAL_ACCOUNT
import com.duduapps.mybanks.util.API_OPERATION
import com.duduapps.mybanks.util.API_PIX_CODE
import com.duduapps.mybanks.util.API_ROUTE_ACCOUNTS
import com.duduapps.mybanks.util.API_ROUTE_ACCOUNT_REGISTER
import com.duduapps.mybanks.util.API_ROUTE_BANKS
import com.duduapps.mybanks.util.API_ROUTE_IDENTIFY
import com.duduapps.mybanks.util.API_SUCCESS
import com.duduapps.mybanks.util.API_TOKEN
import com.duduapps.mybanks.util.API_TYPE
import com.duduapps.mybanks.util.API_VERSION_LAST
import com.duduapps.mybanks.util.API_VERSION_MIN
import com.duduapps.mybanks.util.PREF_ADMOB_AD_MAIN_ID
import com.duduapps.mybanks.util.PREF_ADMOB_INTERSTITIAL_ID
import com.duduapps.mybanks.util.PREF_FCM_TOKEN
import com.duduapps.mybanks.util.PREF_SHARE_LINK
import com.duduapps.mybanks.util.PREF_SHOW_ALERT_LOGIN
import com.duduapps.mybanks.util.appLog
import com.duduapps.mybanks.util.copyToClipboard
import com.duduapps.mybanks.util.getBooleanVal
import com.duduapps.mybanks.util.getIntVal
import com.duduapps.mybanks.util.getValidJSONObject
import com.duduapps.mybanks.util.havePlan
import com.duduapps.mybanks.util.hide
import com.duduapps.mybanks.util.isLogged
import com.duduapps.mybanks.util.loadAdMobBanner
import com.duduapps.mybanks.util.logout
import com.duduapps.mybanks.util.printFuelLog
import com.duduapps.mybanks.util.saveAccounts
import com.duduapps.mybanks.util.saveBanks
import com.duduapps.mybanks.util.show
import com.duduapps.mybanks.util.storeAppLink
import com.duduapps.mybanks.util.unsentAccountsCount
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.orhanobut.hawk.Hawk
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.share
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val realm = Realm.getDefaultInstance()
    private var accounts: MutableList<Account> = mutableListOf()
    private var accountsAdapter: AccountsAdapter? = null
    private var menuLogin: MenuItem? = null
    private var menuLogout: MenuItem? = null
    private var menuSearch: MenuItem? = null
    private var searchView: SearchView? = null
    private var interstitialAd: InterstitialAd? = null
    private var lastTerms: String = ""
    private val registerListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.progress.rlProgressLight.visibility = View.VISIBLE
            }
        }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView != null && !searchView!!.isIconified)
                    searchView?.onActionViewCollapsed()
                else
                    finish()
            }
        })

        initAdMob()
        initViews()
        apiUpdateBanks()
        apiSyncAccounts()
        checkVersion()
    }

    private fun initAdMob() {
        MobileAds.initialize(this) {}

        val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
        MobileAds.setRequestConfiguration(configuration)

        loadAdMobBanner(binding.llBanner, Hawk.get(PREF_ADMOB_AD_MAIN_ID, ""))

        loadInterstitialAd()
    }

    private fun initViews() {
        binding.rvAccounts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        binding.rvAccounts.layoutManager = layoutManager

        accountsAdapter = AccountsAdapter(this)
        binding.rvAccounts.adapter = accountsAdapter

        val divider = DividerItemDecoration(binding.rvAccounts.context, layoutManager.orientation)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.recycler_divider)!!)
        binding.rvAccounts.addItemDecoration(divider)

        binding.rvAccounts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fabShareAll.visibility == View.VISIBLE) {
                    showHideButtons(false)
                } else if (dy < 0 && binding.fabShareAll.visibility != View.VISIBLE) {
                    showHideButtons(true)
                }
            }
        })


        appLog(TAG, "isLogged(): ${isLogged()}")
        appLog(TAG, "PREF_SHOW_ALERT_LOGIN: ${Hawk.put(PREF_SHOW_ALERT_LOGIN, true)}")

        if (!isLogged() && Hawk.put(PREF_SHOW_ALERT_LOGIN, true)) {
            if (realm.where(Account::class.java).isNull("deleted").count() > 0) {
                startActivity(intentFor<AlertLoginActivity>())
            }
        }

        binding.fabCopyAll.setOnClickListener {
            val text = getShareText()

            if (text.isNotEmpty()) {
                copyToClipboard(text)
                toast(R.string.accounts_copied)
            }

            interstitialAd?.show(this)
        }

        binding.fabShareAll.setOnClickListener {
            val text = getShareText()

            if (text.isNotEmpty())
                share(getShareText(), getString(R.string.my_bank_accounts))

            interstitialAd?.show(this)
        }
    }

    override fun onResume() {
        super.onResume()

        renderData()
        apiSyncAccounts()
        searchVisibility()

        menuLogin?.isVisible = !isLogged()
        menuLogout?.isVisible = isLogged()
    }

    private fun getAccounts(terms: String = ""): MutableList<Account> {
        var items: MutableList<Account>? = null

        if (!realm.isClosed) {
            val query = realm.where(Account::class.java)
                .isNull("deleted")

            if (terms.isNotEmpty())
                query.contains("label", terms, Case.INSENSITIVE)

            query.sort("label", Sort.ASCENDING)

            items = query.findAll()
        }

        return items ?: mutableListOf()
    }

    private fun searchVisibility() {
        val size = getAccounts("").size
        menuSearch?.isVisible = size > 3
    }

    private fun renderData() {
        accounts = getAccounts(lastTerms)

        if (accounts.size == 0) {

            binding.rvAccounts.visibility = View.GONE
            binding.llEmpty.visibility = View.VISIBLE

            binding.tvAlertBackup.visibility = if (isLogged()) View.GONE else View.VISIBLE

            if (lastTerms.isEmpty()) {
                binding.btAddAccount.setOnClickListener {
                    startActivity(intentFor<CreateAccountActivity>())
                }

                if (isLogged()) {
                    binding.btLogin.hide()
                } else {
                    binding.btLogin.show()
                    binding.btLogin.setOnClickListener {
                        val intent = Intent(this, LoginActivity::class.java)
                        registerListener.launch(intent)
                    }
                }

            }

            showHideButtons(false)

        } else {

            binding.rvAccounts.visibility = View.VISIBLE
            binding.llEmpty.visibility = View.GONE

            accountsAdapter?.setData(accounts)

            showHideButtons(true)

        }
    }

    private fun getShareText(): String {
        var fullText = ""

        if (accounts.size > 0) {
            accounts.forEach { account ->
                if (fullText.isNotEmpty())
                    fullText += "\n--\n"

                fullText += getString(R.string.label_bank, account.bankName()) + "\n"
                fullText += getString(R.string.label_agency, account.agency) + "\n"
                fullText += getString(R.string.label_account, account.account) + "\n"
                if (account.operation.isNotEmpty())
                    fullText += getString(R.string.label_operation, account.operation) + "\n"
                fullText += getString(R.string.label_type, account.type) + "\n"
                fullText += getString(R.string.label_holder, account.holder) + "\n"
                fullText += getString(R.string.label_cpf, account.document)
            }
        }

        return fullText
    }

    private fun checkVersion() {
        val token = Hawk.get(PREF_FCM_TOKEN, "")

        if (/*isLogged() && */token.isNotEmpty()) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    if (apiObj.getBooleanVal(API_SUCCESS)) {
                        val versionLast = apiObj.getIntVal(API_VERSION_LAST)
                        val versionMin = apiObj.getIntVal(API_VERSION_MIN)

                        if (BuildConfig.VERSION_CODE < versionMin) {
                            alert(
                                getString(R.string.update_needed),
                                getString(R.string.updated_title)
                            ) {
                                positiveButton(R.string.updated_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.updated_logout) { finish() }
                                onCancelled { finish() }
                            }.show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            alert(
                                getString(R.string.update_available),
                                getString(R.string.updated_title)
                            ) {
                                positiveButton(R.string.updated_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.updated_negative) {}
                            }.show()
                        }
                    }
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        menuLogin = menu?.findItem(R.id.action_login)
        menuLogout = menu?.findItem(R.id.action_logout)
        menuSearch = menu?.findItem(R.id.action_search)

        menuLogin?.isVisible = !isLogged()
        menuLogout?.isVisible = isLogged()

        searchView = menuSearch?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView?.clearFocus()
                return true
            }

            override fun onQueryTextChange(terms: String): Boolean = doneSearch(terms)
        })

        searchVisibility()

        return true
    }

    fun doneSearch(terms: String): Boolean {
        if (accountsAdapter != null) {
            lastTerms = terms

            renderData()

            if (terms.isNotEmpty())
                return true
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_account -> {
                startActivity(intentFor<CreateAccountActivity>())
                interstitialAd?.show(this)
                true
            }

            R.id.action_login -> {
                startActivity(intentFor<LoginActivity>())
                true
            }

            R.id.action_logout -> {
                logout()
                true
            }

            R.id.action_remove_ads -> {
                startActivity(intentFor<RemoveAdsActivity>())
                true
            }

            R.id.action_share -> {
                val subject = getString(R.string.share_subject)
                val link = Hawk.get(PREF_SHARE_LINK, storeAppLink())
                val body = getString(R.string.share_text, link)
                share(body, subject)
                true
            }

            R.id.action_rate -> {
                browse(storeAppLink())
                true
            }

            R.id.action_send_feedback -> {
                startActivity(intentFor<SendFeedbackActivity>())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showHideButtons(showButtons: Boolean) {
        if (showButtons) {
            binding.fabCopyAll.show()
            binding.fabShareAll.show()
        } else {
            binding.fabCopyAll.hide()
            binding.fabShareAll.hide()
        }
    }

    private fun apiUpdateBanks() {
        API_ROUTE_BANKS.httpGet().responseString { request, response, result ->
            printFuelLog(request, response, result)

            realm.saveBanks(result)
        }
    }

    private fun apiSyncAccounts() {

        val account = realm.where(Account::class.java)
            .equalTo("synced", false)
            .findFirst()

        if (account != null) {

            val params = listOf(
                API_ID to account.id,
                API_PIX_CODE to account.pixCode,
                API_BANK_ID to account.bankId,
                API_LABEL to account.label,
                API_TYPE to account.type,
                API_AGENCY to account.agency,
                API_ACCOUNT to account.account,
                API_OPERATION to account.operation,
                API_HOLDER to account.holder,
                API_DOCUMENT to account.document,
                API_LEGAL_ACCOUNT to account.legalAccount,
                API_DELETED to account.deleted
            )

            API_ROUTE_ACCOUNT_REGISTER.httpPost(params)
                .responseString { request, response, result ->
                    printFuelLog(request, response, result)

                    val success = realm.saveAccounts(result)

                    if (success) {

                        realm.executeTransaction {
                            account.deleteFromRealm()
                        }

                        renderData()

                        if (realm.unsentAccountsCount() > 0)
                            apiSyncAccounts()
                        else
                            apiUpdateAccounts()

                    } else {

                        apiUpdateAccounts()

                    }
                }

        } else {

            apiUpdateAccounts()

        }
    }

    private fun apiUpdateAccounts() {
        binding.progress.rlProgressLight.show()

        if (isLogged() && realm.unsentAccountsCount() == 0L) {
            API_ROUTE_ACCOUNTS.httpGet().responseString { request, response, result ->
                printFuelLog(request, response, result)

                val success = realm.saveAccounts(result)

                if (success)
                    renderData()

                binding.progress.rlProgressLight.hide()
            }
        } else {
            binding.progress.rlProgressLight.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm?.close()
    }

    private fun loadInterstitialAd() {
        val logTag = "InterstitialAd"
        val adUnitId = Hawk.get(PREF_ADMOB_INTERSTITIAL_ID, "")

        if (adUnitId.isNotEmpty() && !havePlan()) {
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    appLog(logTag, "onAdFailedToLoad(): ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    appLog(logTag, "Ad was loaded")
                    interstitialAd = ad
                }
            })
        }
    }

}
