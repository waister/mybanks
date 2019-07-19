package com.duduapps.mybanks.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.util.*
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.inc_toolbar.*
import org.jetbrains.anko.*

class AccountDetailsActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private lateinit var account: Account
    private var accountId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountId = intent.getLongExtra(PARAM_ID, 0L)

    }

    override fun onResume() {
        super.onResume()

        val firstAccount = realm.where(Account::class.java)
            .isNull("deleted")
            .equalTo("id", accountId)
            .findFirst()

        if (firstAccount != null) {

            account = firstAccount

            supportActionBar?.title = account.label

            renderData()

            fab_copy.setOnClickListener {
                copyToClipboard(getShareText())
                toast(R.string.account_copied)
            }

            fab_share.setOnClickListener {
                share(getShareText(), getString(R.string.my_bank_account))
            }

        } else {

            longToast(R.string.error_account_not_found)
            finish()

        }
    }

    private fun renderData() {
        tv_bank_name.text = getString(R.string.label_bank, account.bank!!.name, account.bank!!.code)
        tv_agency.text = getString(R.string.label_agency, account.agency)
        tv_account.text = getString(R.string.label_account, account.account)
        tv_operation.text = getString(R.string.label_account, account.account)
        tv_type.text = getString(R.string.label_type, account.type)
        tv_holder.text = getString(R.string.label_holder, account.holder)

        if (account.legalAccount)
            tv_document.text = getString(R.string.label_cnpj, account.document)
        else
            tv_document.text = getString(R.string.label_cpf, account.document)

        if (account.operation.isEmpty())
            tv_operation.visibility = View.GONE

        tv_bank_name.setOnClickListener { copyItem(account.bank!!.code) }
        tv_agency.setOnClickListener { copyItem(account.agency) }
        tv_account.setOnClickListener { copyItem(account.account) }
        tv_operation.setOnClickListener { copyItem(account.operation) }
        tv_type.setOnClickListener { copyItem(account.type) }
        tv_holder.setOnClickListener { copyItem(account.holder) }
        tv_document.setOnClickListener { copyItem(account.document) }

        loadAdBanner(Hawk.get(PREF_ADMOB_AD_MAIN_ID, ""), ll_buttons)
    }

    private fun copyItem(text: String) {
        copyToClipboard(text)
        toast(R.string.data_copied)
    }

    private fun getShareText(): String {
        var fullText = ""

        fullText += getString(R.string.label_bank, account.bank!!.name, account.bank!!.code) + "\n"
        fullText += getString(R.string.label_agency, account.agency) + "\n"
        fullText += getString(R.string.label_account, account.account) + "\n"
        if (account.operation.isNotEmpty())
            fullText += getString(R.string.label_operation, account.operation) + "\n"
        fullText += getString(R.string.label_type, account.type) + "\n"
        fullText += getString(R.string.label_holder, account.holder) + "\n"
        fullText += getString(R.string.label_cpf, account.document)

        return fullText
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_edit -> {
                startActivity(intentFor<CreateAccountActivity>(PARAM_ID to account.id))
                true
            }
            R.id.action_delete -> {
                alert(R.string.confirm_deleted_account, R.string.confirmation) {
                    positiveButton(R.string.confirm) {}
                    negativeButton(R.string.cancel) {
                        realm.executeTransaction {
                            account.updated = currentTimestamp()
                            account.deleted = currentTimestamp()
                            account.synced = false

                            realm.copyToRealmOrUpdate(account)

                            longToast(R.string.success_account_deleted)

                            finish()
                        }
                    }
                }.show()
                true
            }
            R.id.action_remove_ads -> {
                startActivity(intentFor<RemoveAdsActivity>())
                true
            }
            else -> {
                onBackPressed()
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm?.close()
    }

}
