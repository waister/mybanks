package com.duduapps.mybanks.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.util.*
import com.google.android.gms.ads.AdSize
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account_details.*
import org.jetbrains.anko.*

class AccountDetailsActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private lateinit var account: Account
    private var accountId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountId = intent.getLongExtra(PARAM_ID, 0L)

        val adMobId = Hawk.get(PREF_ADMOB_AD_MAIN_ID, "")
        loadAdBanner(ll_banner, adMobId, AdSize.MEDIUM_RECTANGLE)
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

            bt_copy.setOnClickListener {
                copyToClipboard(getShareText())
                toast(R.string.account_copied)
            }

            bt_share.setOnClickListener {
                share(getShareText(), getString(R.string.my_bank_account))
            }

        } else {

            longToast(R.string.error_account_not_found)
            finish()

        }
    }

    private fun renderData() {
        tv_pix_code.printLine(getString(R.string.label_pix_code, account.pixCode), account.pixCode)
        tv_bank_name.printLine(getString(R.string.label_bank, account.bankName()), account.bank!!.code)
        tv_agency.printLine(getString(R.string.label_agency, account.agency), account.agency)
        tv_account.printLine(getString(R.string.label_account, account.account), account.account)
        tv_operation.printLine(getString(R.string.label_account, account.operation), account.operation)
        tv_type.printLine(getString(R.string.label_type, account.type), account.type)
        tv_holder.printLine(getString(R.string.label_holder, account.holder), account.holder)

        if (account.legalAccount)
            tv_document.printLine(getString(R.string.label_cnpj, account.document), account.document)
        else
            tv_document.printLine(getString(R.string.label_cpf, account.document), account.document)
    }

    private fun TextView.printLine(label: String, value: String?) {
        if (value != null && value.isNotEmpty()) {
            this.text = label
            this.setOnClickListener { copyItem(value) }
        } else {
            this.visibility = View.GONE
        }
    }

    private fun copyItem(text: String) {
        copyToClipboard(text)
        toast(R.string.data_copied)
    }

    private fun getShareText(): String {
        var fullText = ""

        fullText += getString(R.string.label_pix_code, account.pixCode) + "\n"
        fullText += getString(R.string.label_bank, account.bankName()) + "\n"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                startActivity(intentFor<CreateAccountActivity>(PARAM_ID to account.id))
                true
            }
            R.id.action_delete -> {
                alert(R.string.confirm_deleted_account, R.string.confirmation) {
                    positiveButton(R.string.confirm) {
                        realm.executeTransaction {
                            account.updated = currentTimestamp()
                            account.deleted = currentTimestamp()
                            account.synced = false

                            realm.copyToRealmOrUpdate(account)

                            longToast(R.string.success_account_deleted)

                            finish()
                        }
                    }
                    negativeButton(R.string.cancel) {}
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
