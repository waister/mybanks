package com.duduapps.mybanks.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.databinding.ActivityAccountDetailsBinding
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.util.PARAM_ID
import com.duduapps.mybanks.util.PREF_ADMOB_AD_MAIN_ID
import com.duduapps.mybanks.util.copyToClipboard
import com.duduapps.mybanks.util.currentTimestamp
import com.duduapps.mybanks.util.loadAdMobBanner
import com.google.android.gms.ads.AdSize
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.share
import org.jetbrains.anko.toast

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailsBinding

    private val realm = Realm.getDefaultInstance()
    private lateinit var account: Account
    private var accountId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountId = intent.getLongExtra(PARAM_ID, 0L)

        val adMobId = Hawk.get(PREF_ADMOB_AD_MAIN_ID, "")
        loadAdMobBanner(binding.llBanner, adMobId, AdSize.MEDIUM_RECTANGLE)
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

            binding.btCopy.setOnClickListener {
                copyToClipboard(getShareText())
                toast(R.string.account_copied)
            }

            binding.btShare.setOnClickListener {
                share(getShareText(), getString(R.string.my_bank_account))
            }

        } else {

            longToast(R.string.error_account_not_found)
            finish()

        }
    }

    private fun renderData() = with(binding) {
        tvPixCode.printLine(getString(R.string.label_pix_code, account.pixCode), account.pixCode)
        tvBankName.printLine(
            getString(R.string.label_bank, account.bankName()),
            account.bank!!.code
        )
        tvAgency.printLine(getString(R.string.label_agency, account.agency), account.agency)
        tvAccount.printLine(getString(R.string.label_account, account.account), account.account)
        tvOperation.printLine(
            getString(R.string.label_account, account.operation),
            account.operation
        )
        tvType.printLine(getString(R.string.label_type, account.type), account.type)
        tvHolder.printLine(getString(R.string.label_holder, account.holder), account.holder)

        if (account.legalAccount)
            tvDocument.printLine(getString(R.string.label_cnpj, account.document), account.document)
        else
            tvDocument.printLine(getString(R.string.label_cpf, account.document), account.document)
    }

    private fun TextView.printLine(label: String, value: String?) {
        if (!value.isNullOrEmpty()) {
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
                onBackPressedDispatcher.onBackPressed()
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm?.close()
    }

}
