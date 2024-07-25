package com.duduapps.mybanks.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.databinding.ActivityCreateAccountBinding
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.model.Bank
import com.duduapps.mybanks.util.PARAM_ID
import com.duduapps.mybanks.util.PREF_LAST_DOCUMENT
import com.duduapps.mybanks.util.PREF_LAST_HOLDER
import com.duduapps.mybanks.util.PREF_LAST_LEGAL_ACCOUNT
import com.duduapps.mybanks.util.currentTimestamp
import com.duduapps.mybanks.util.getNumbers
import com.duduapps.mybanks.util.hideKeyboard
import com.duduapps.mybanks.util.isVisible
import com.duduapps.mybanks.util.setEmpty
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.okButton

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding

    private val realm = Realm.getDefaultInstance()
    private var banks: MutableList<Bank> = mutableListOf()
    private var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.btSubmit.setOnClickListener { submitCreate() }

        initViews()
    }

    private fun initViews() = with(binding) {
        if (account == null) {
            val isLegalAccount = Hawk.get(PREF_LAST_LEGAL_ACCOUNT, false) ?: false
            val document = Hawk.get(PREF_LAST_DOCUMENT, "") ?: ""

            cbLegalAccount.isChecked = isLegalAccount
            etHolder.setText(Hawk.get(PREF_LAST_HOLDER, ""))

            toggleDocument()

            if (isLegalAccount)
                etCnpj.setText(document)
            else
                etCpf.setText(document)
        }

        cbLegalAccount.setOnCheckedChangeListener { _, isChecked ->
            toggleDocument()

            if (isChecked)
                etCnpj.requestFocus()
            else
                etCpf.requestFocus()
        }

        etCpf.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitCreate()
                true
            } else false
        }

        etCnpj.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitCreate()
                true
            } else false
        }

        etAccount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text != null) {
                    val numbers = text.toString().getNumbers()
                    val length = numbers.length

                    if (length >= 2) {
                        val account = numbers.take(length - 1)
                        val digit = numbers.takeLast(1)

                        etAccount.removeTextChangedListener(this)
                        text.replace(0, text.length, "$account-$digit")
                        etAccount.addTextChangedListener(this)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        populateBanks()
    }

    private fun toggleDocument() = with(binding) {
        lyCpf.isVisible(!cbLegalAccount.isChecked)
        lyCnpj.isVisible(cbLegalAccount.isChecked)
    }

    private fun renderEditData() = with(binding) {
        val item = account!!

        supportActionBar?.title = getString(R.string.label_edit_account, item.label)

        val bankFullName = "${item.bank!!.code} - ${item.bank!!.name}"

        etBank.setText(bankFullName)
        etLabel.setText(item.label)
        etPixCode.setText(item.pixCode)
        etAgency.setText(item.agency)
        etAccount.setText(item.account)
        etOperation.setText(item.operation)
        etHolder.setText(item.holder)

        cbLegalAccount.isChecked = item.legalAccount

        if (item.legalAccount)
            etCnpj.setText(item.document)
        else
            etCpf.setText(item.document)

        toggleDocument()

        when (item.type) {
            getString(R.string.checking) -> rbChecking.isChecked = true
            getString(R.string.savings) -> rbSavings.isChecked = true
        }

        btSubmit.setText(R.string.save_changes)
    }

    private fun populateBanks() {
        val banksList = mutableListOf<String>()

        banks.forEach {
            banksList.add("${it.code} - ${it.name}")
        }

        val adapter = ArrayAdapter(this, R.layout.item_spinner_list, banksList)
        binding.etBank.threshold = 1
        binding.etBank.setAdapter(adapter)
    }

    private fun submitCreate() = with(binding) {
        etLabel.hideKeyboard()

        var bankId = 0
        val bankName = etBank.text.toString()
        val label = etLabel.text.toString()
        val pixCode = etPixCode.text.toString()
        val agency = etAgency.text.toString()
        val number = etAccount.text.toString()
        val operation = etOperation.text.toString()
        val holder = etHolder.text.toString()
        val legalAccount = cbLegalAccount.isChecked
        val document = (if (legalAccount) etCnpj.text else etCpf.text).toString()

        val type = when (rgType.checkedRadioButtonId) {
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
            etLabel.error = getString(R.string.error_label)
        }
        if (bankId == 0) {
            errors++
            etBank.error = getString(R.string.error_bank)
        }
        if (agency.isEmpty()) {
            errors++
            etAgency.error = getString(R.string.error_agency)
        } else if (agency.length < 4) {
            errors++
            etAgency.error = getString(R.string.error_agency_invalid)
        }
        if (number.isEmpty()) {
            errors++
            etAccount.error = getString(R.string.error_account)
        } else if (number.length < 3) {
            errors++
            etAccount.error = getString(R.string.error_account_invalid)
        }
        if (holder.isEmpty()) {
            errors++
            etHolder.error = getString(R.string.error_holder)
        }
        if (document.isEmpty()) {
            errors++

            if (legalAccount)
                etCnpj.error = getString(R.string.error_cnpj)
            else
                etCpf.error = getString(R.string.error_cpf)
        }

        if (errors == 0) {
            if (type.isEmpty()) {
                alert(R.string.error_type, R.string.ops) { okButton {} }.show()
            } else {

                Hawk.put(PREF_LAST_HOLDER, holder)
                Hawk.put(PREF_LAST_DOCUMENT, document)
                Hawk.put(PREF_LAST_LEGAL_ACCOUNT, cbLegalAccount.isChecked)

                val item = Account()

                if (account != null) {
                    item.id = account!!.id
                } else {
                    item.id = System.currentTimeMillis()
                    item.created = currentTimestamp()
                }

                item.pixCode = pixCode
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

                if (account != null) {

                    longToast(R.string.success_account_edited)
                    finish()

                } else {

                    etBank.setEmpty()
                    etLabel.setEmpty()
                    etPixCode.setEmpty()
                    etAgency.setEmpty()
                    etAccount.setEmpty()
                    rbChecking.isChecked = true
                    rbSavings.isChecked = false

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
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        realm?.close()
    }

}
