package com.duduapps.mybanks.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.model.Bank
import com.duduapps.mybanks.util.*
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_create_account.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.okButton

class CreateAccountActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private var banks: MutableList<Bank> = mutableListOf()
    private var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val accountId = intent.getLongExtra(PARAM_ID, 0L)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (accountId > 0) {

            account = realm.where(Account::class.java)
                .isNull("deleted")
                .equalTo("id", accountId)
                .findFirst()

            if (account != null) {

                renderEditData()

            } else {

                longToast(R.string.error_account_not_found)
                finish()

            }

        }

        banks = realm.where(Bank::class.java).findAll()

        bt_submit.setOnClickListener { submitCreate() }

        initViews()
    }

    private fun initViews() {
        if (account == null) {
            cb_legal_account.isChecked = Hawk.get(PREF_LAST_LEGAL_ACCOUNT, false)
            et_holder.setText(Hawk.get(PREF_LAST_HOLDER, ""))
            et_document.setText(Hawk.get(PREF_LAST_DOCUMENT, ""))
        }

        cb_legal_account.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ly_document.hint = getString(R.string.cnpj)
                et_document.mask = "##.###.###/####-##"
            } else {
                ly_document.hint = getString(R.string.cpf)
                et_document.mask = "###.###.###-##"
            }

            et_document.setText("")
        }

        et_document.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitCreate()
                true
            } else false
        }

        et_account.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text != null) {
                    val numbers = text.toString().getNumbers()
                    val length = numbers.length

                    if (length >= 2) {
                        val account = numbers.take(length - 1)
                        val digit = numbers.takeLast(1)

                        et_account.removeTextChangedListener(this)
                        text.replace(0, text.length, "$account-$digit")
                        et_account.addTextChangedListener(this)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        populateBanks()
    }

    private fun renderEditData() {
        val item = account!!

        supportActionBar?.title = getString(R.string.label_edit_account, item.label)

        val bankFullName = "${item.bank!!.code} - ${item.bank!!.name}"

        cb_legal_account.isChecked = item.legalAccount

        et_bank.setText(bankFullName)
        et_label.setText(item.label)
        et_agency.setText(item.agency)
        et_account.setText(item.account)
        et_operation.setText(item.operation)
        et_holder.setText(item.holder)
        et_document.setText(item.document)

        when (item.type) {
            getString(R.string.checking) -> rb_checking.isChecked = true
            getString(R.string.savings) -> rb_savings.isChecked = true
        }

        bt_submit.setText(R.string.save_changes)
    }

    private fun populateBanks() {
        val banksList = mutableListOf<String>()

        banks.forEach {
            banksList.add("${it.code} - ${it.name}")
        }

        val adapter = ArrayAdapter(this, R.layout.item_spinner_list, banksList)
        et_bank.threshold = 1
        et_bank.setAdapter(adapter)
    }

    private fun submitCreate() {
        et_document.hideKeyboard()

        var bankId = 0
        val bankName = et_bank.text.toString()
        val label = et_label.text.toString()
        val agency = et_agency.text.toString()
        val number = et_account.text.toString()
        val operation = et_operation.text.toString()
        val holder = et_holder.text.toString()
        val document = et_document.text.toString()
        val legalAccount = cb_legal_account.isChecked

        val type = when (rg_type.checkedRadioButtonId) {
            R.id.rb_checking -> getString(R.string.checking)
            R.id.rb_savings -> getString(R.string.savings)
            else -> ""
        }

        if (bankName.contains(" - ")) {
            val parts = bankName.split(" - ")
            banks.forEach {
                if (parts[0] == it.code)
                    bankId = it.id
            }
        }

        var errors = 0

        if (label.isEmpty()) {
            errors++
            et_label.error = getString(R.string.error_label)
        }
        if (bankId == 0) {
            errors++
            et_bank.error = getString(R.string.error_bank)
        }
        if (agency.isEmpty()) {
            errors++
            et_agency.error = getString(R.string.error_agency)
        } else if (agency.length < 4) {
            errors++
            et_agency.error = getString(R.string.error_agency_invalid)
        }
        if (number.isEmpty()) {
            errors++
            et_account.error = getString(R.string.error_account)
        } else if (number.length < 3) {
            errors++
            et_account.error = getString(R.string.error_account_invalid)
        }
        if (holder.isEmpty()) {
            errors++
            et_holder.error = getString(R.string.error_holder)
        }
        if (document.isEmpty()) {
            errors++
            et_document.error = getString(R.string.error_document)
        }

        if (errors == 0) {
            if (type.isEmpty()) {
                alert(R.string.error_type, R.string.ops) { okButton {} }.show()
            } else {

                Hawk.put(PREF_LAST_HOLDER, holder)
                Hawk.put(PREF_LAST_DOCUMENT, document)
                Hawk.put(PREF_LAST_LEGAL_ACCOUNT, cb_legal_account.isChecked)

                val item = Account()

                if (account != null) {
                    item.id = account!!.id
                } else {
                    item.id = System.currentTimeMillis()
                    item.created = currentTimestamp()
                }

                item.bankId = bankId
                item.label = label
                item.agency = agency
                item.account = number
                item.type = type
                item.holder = holder
                item.document = document
                item.legalAccount = legalAccount
                item.operation = operation
                item.updated = currentTimestamp()
                item.deleted = null
                item.synced = false

                item.bank = realm.where(Bank::class.java).equalTo("id", bankId).findFirst()!!

                realm.executeTransaction {
                    realm.copyToRealmOrUpdate(item)
                }

                if (this.account != null) {

                    longToast(R.string.success_account_edited)
                    finish()

                } else {

                    et_bank.setText("")
                    et_label.setText("")
                    et_agency.setText("")
                    et_account.setText("")
                    rb_checking.isChecked = true
                    rb_savings.isChecked = false

                    alert(R.string.success_account_added, R.string.success) {
                        positiveButton(R.string.finish) { finish() }
                        negativeButton(R.string.register_new) {}
                        onCancelled { finish() }
                    }.show()

                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        realm?.close()
    }

}
