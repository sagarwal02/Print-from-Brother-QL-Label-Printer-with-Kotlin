package com.brother.ptouch.sdk.printdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.brother.ptouch.sdk.LabelInfo
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.printdemo.common.Common
import com.brother.ptouch.sdk.printdemo.common.MsgDialog
import com.brother.ptouch.sdk.printdemo.common.MsgHandle
import com.brother.ptouch.sdk.printdemo.printprocess.TemplatePrint
import java.util.ArrayList
import java.util.HashMap

class Printing : BaseActivity() {

    private val listItems = ArrayList<HashMap<String, Any>>()

    private var currentInput = false
    // Called when Template key's data has been changed
    private val watcher = object : TextWatcher {

        override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                   count: Int) {

        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {

        }

        /**
         * Called when Template key's data has been changed
         */
        override fun afterTextChanged(arg0: Editable) {

            if (currentInput) {
                addEndFlg()
                showInputData()
            }
        }
    }
    private var mSpinnerEncoding: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.printstuff)

        mDialog = MsgDialog(this)
        mHandle = MsgHandle(this, mDialog)
        myPrint = TemplatePrint(this, mHandle, mDialog)

        // when use bluetooth print set the adapter
        val bluetoothAdapter = super.getBluetoothAdapter()
        myPrint.setBluetoothAdapter(bluetoothAdapter)

        // set index & ObjectName EditView invisible
        val layoutIndex = findViewById(R.id.LinearLayoutIndex) as LinearLayout
        val layoutObjectName = findViewById(R.id.LinearLayoutObjectName) as LinearLayout
        layoutIndex.visibility = TableLayout.GONE
        layoutObjectName.visibility = TableLayout.GONE


        val addButton = findViewById(R.id.addButton) as Button
        addButton.setOnClickListener { addOnClick() }

        val deleteButton = findViewById(R.id.deleteButton) as Button
        deleteButton.setOnClickListener { deleteOnClick() }

        val nextPrintButton = findViewById(R.id.nextPrintButton) as Button
        nextPrintButton.setOnClickListener { nextTemplatePrintOnClick() }


        val btnPrinterSettings = findViewById(R.id.btnPrinterSettings) as Button
        btnPrinterSettings.setOnClickListener { printerSettingsButtonOnClick() }


        val btnPrint = findViewById(R.id.btnPrint) as Button
        btnPrint.setOnClickListener { printButtonOnClick() }
        btnPrint.isEnabled = false

        // initialization for RadioGroup
        val radioGroup = this
                .findViewById(R.id.radioGroupForReplaceText) as RadioGroup
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val layoutIndex = findViewById(R.id.LinearLayoutIndex) as LinearLayout
            val layoutObjectName = findViewById(R.id.LinearLayoutObjectName) as LinearLayout

            when (checkedId) {
                R.id.radio0 // for replaceText
                -> {
                    layoutIndex.visibility = TableLayout.GONE
                    layoutObjectName.visibility = TableLayout.GONE
                }
                R.id.radio1 // for replaceTextIndex
                -> {
                    layoutIndex.visibility = TableLayout.VISIBLE
                    layoutObjectName.visibility = TableLayout.GONE
                }
                R.id.radio2 // for replaceTextName
                -> {
                    layoutIndex.visibility = TableLayout.GONE
                    layoutObjectName.visibility = TableLayout.VISIBLE
                }
                else -> {
                }
            }
        }

        // add listener to EditTextView of [Template key]
        (this.findViewById(R.id.edtTemplateKey) as EditText)
                .addTextChangedListener(watcher)
        mSpinnerEncoding = findViewById(R.id.spinnerEncoding) as Spinner

        val data = arrayOfNulls<String>(3)
        data[0] = Common.ENCODING_ENG
        data[1] = Common.ENCODING_JPN
        data[2] = Common.ENCODING_CHN

        // set the pages info. to display
        val adapter = ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinnerEncoding!!.adapter = adapter
        mSpinnerEncoding!!.setSelection(0)

    }

    /**
     * Called when [Add] button is tapped
     */
    private fun addOnClick() {

        if (!checkNewInputData()) {
            if (!currentInput) {
                addStartFlg()
            }

            setNewInputData()
            showInputData()
        }
    }

    /**
     * Called when [Delete] button is tapped
     */
    private fun deleteOnClick() {

        val lastItemIndex = listItems.size - 1

        // delete the latest input data
        if (lastItemIndex == 1) {
            listItems.clear()
            currentInput = false
        } else if (lastItemIndex > 1) {

            var mapData: Map<String, Any>
            mapData = listItems[lastItemIndex]

            if (Integer.parseInt(mapData[Common.TEMPLATE_REPLACE_TYPE]
                            .toString()) == Common.TEMPLATE_REPLACE_TYPE_END) {
                currentInput = true
            } else if (Integer.parseInt(mapData[Common.TEMPLATE_REPLACE_TYPE].toString()) == Common.TEMPLATE_REPLACE_TYPE_START) {
                currentInput = false
            }

            listItems.removeAt(lastItemIndex)

            // delete the start flag if it is the first input data in each
            // template's input data
            mapData = listItems[lastItemIndex - 1]
            if (Integer.parseInt(mapData[Common.TEMPLATE_REPLACE_TYPE]
                            .toString()) == Common.TEMPLATE_REPLACE_TYPE_START) {
                currentInput = false
                listItems.removeAt(lastItemIndex - 1)
            }
        }

        // update the input data for screen display
        showInputData()
    }

    /**
     * Called when [Next Print] button is tapped
     */
    private fun nextTemplatePrintOnClick() {

        // set the end flag and refresh the display
        if (currentInput) {
            addEndFlg()
            showInputData()
        }
    }

    /**
     * Called when [Print] button is tapped
     */
    override fun printButtonOnClick() {
        if (!checkUSB())
            return
        // set the end flag and refresh the display
        if (currentInput) {
            addEndFlg()
            showInputData()
        }

        // do the print
        if (listItems.size > 0) {

            (myPrint as TemplatePrint).setEncoding(mSpinnerEncoding!!
                    .selectedItem as String)

            (myPrint as TemplatePrint).setPrintData(listItems)
            myPrint.print()
        }
    }

    /**
     * Check the input data. Show error if no index or object name is input.
     */
    private fun checkNewInputData(): Boolean {

        val radioGroup = this
                .findViewById(R.id.radioGroupForReplaceText) as RadioGroup
        var errorInput = false

        when (radioGroup.checkedRadioButtonId) {
            R.id.radio0 // for replaceText
            -> {
            }

            R.id.radio1 // for replaceTextIndex
            -> {
                val index = (findViewById(R.id.edtIndex) as EditText).text
                        .toString()

                // error if no index is input
                if (index.equals("", ignoreCase = true)) {
                    errorInput = true
                }
            }

            R.id.radio2 // for replaceTextName
            -> {
                val objectName = (findViewById(R.id.edtObjectName) as EditText)
                        .text.toString()

                // error if no object name is input
                if (objectName.equals("", ignoreCase = true)) {
                    errorInput = true
                }
            }

            else -> {
            }
        }

        // show the wrong input message
        if (errorInput) {
            mDialog.showAlertDialog(getString(R.string.msg_title_warning),
                    getString(R.string.error_input))
        }

        return errorInput
    }

    /**
     * Store the input data for printing
     */
    private fun setNewInputData() {

        val radioGroup = this
                .findViewById(R.id.radioGroupForReplaceText) as RadioGroup
        val mapData = HashMap<String, Any>()

        when (radioGroup.checkedRadioButtonId) {
            R.id.radio0 // for replaceText
            -> mapData[Common.TEMPLATE_REPLACE_TYPE] = Common.TEMPLATE_REPLACE_TYPE_TEXT

            R.id.radio1 // for replaceTextIndex
            -> {
                mapData[Common.TEMPLATE_REPLACE_TYPE] = Common.TEMPLATE_REPLACE_TYPE_INDEX
                val index = (findViewById(R.id.edtIndex) as EditText).text
                        .toString()

                mapData[Common.TEMPLATE_OBJECTNAME_INDEX] = index
            }

            R.id.radio2 // for replaceTextName
            -> {
                mapData[Common.TEMPLATE_REPLACE_TYPE] = Common.TEMPLATE_REPLACE_TYPE_NAME
                val objectName = (findViewById(R.id.edtObjectName) as EditText)
                        .text.toString()

                mapData[Common.TEMPLATE_OBJECTNAME_INDEX] = objectName
            }

            else -> {
            }
        }

        val edtTextForReplace = findViewById(R.id.edtTextForReplace) as EditText
        mapData[Common.TEMPLATE_REPLACE_TEXT] = edtTextForReplace.text
                .toString()

        listItems.add(mapData)

    }

    /**
     * Add start flag for multiple pdz's print
     */
    private fun addStartFlg() {

        val key = getTemplateKey()

        if (!key.equals("", ignoreCase = true)) {
            val mapData = HashMap<String, Any>()
            mapData[Common.TEMPLATE_REPLACE_TYPE] = Common.TEMPLATE_REPLACE_TYPE_START

            mapData[Common.TEMPLATE_KEY] = key
            listItems.add(mapData)
            currentInput = true
        }
    }

    /**
     * Add end flag for multiple pdz's print
     */
    private fun addEndFlg() {

        val mapData = HashMap<String, Any>()
        mapData[Common.TEMPLATE_REPLACE_TYPE] = Common.TEMPLATE_REPLACE_TYPE_END
        listItems.add(mapData)
        currentInput = false
    }

    /**
     * Get the template key
     */
    private fun getTemplateKey(): String {

        val strKey = (this.findViewById(R.id.edtTemplateKey) as EditText)
                .text.toString()
        if (strKey.equals("", ignoreCase = true)) {
            mDialog.showAlertDialog(getString(R.string.msg_title_warning),
                    getString(R.string.error_input))
        }

        return strKey
    }

    /**
     * set the input data for replace
     */
    private fun showInputData() {

        val count = listItems.size
        var strInputData = ""

        for (i in 0 until count) {
            val map: Map<String, Any>
            map = listItems[i]

            when (Integer.parseInt(map[Common.TEMPLATE_REPLACE_TYPE]
                    .toString())) {
                Common.TEMPLATE_REPLACE_TYPE_TEXT // for replaceText
                -> strInputData = (strInputData + "Text:"
                        + map[Common.TEMPLATE_REPLACE_TEXT].toString()
                        + "\n")
                Common.TEMPLATE_REPLACE_TYPE_INDEX // for replaceTextIndex
                -> strInputData = (strInputData + "Index:"
                        + map[Common.TEMPLATE_OBJECTNAME_INDEX].toString()
                        + " " + "Text:"
                        + map[Common.TEMPLATE_REPLACE_TEXT].toString()
                        + "\n")
                Common.TEMPLATE_REPLACE_TYPE_NAME // for replaceTextName
                -> strInputData = (strInputData + "ObjectName:"
                        + map[Common.TEMPLATE_OBJECTNAME_INDEX].toString()
                        + " " + "Text:"
                        + map[Common.TEMPLATE_REPLACE_TEXT].toString()
                        + "\n")

                Common.TEMPLATE_REPLACE_TYPE_START // for replaceText
                -> strInputData = (strInputData + "Start template key:"
                        + map[Common.TEMPLATE_KEY].toString() + "\n")
                Common.TEMPLATE_REPLACE_TYPE_END // for replaceText
                -> strInputData = strInputData + "End \n"
                else -> {
                }
            }
        }

        val tvInputData = findViewById(R.id.tvInputData) as TextView
        tvInputData.text = strInputData

        // set the [print] button enable if the replace data is input, otherwise
        // disable
        if (count > 1) {
            findViewById<Button>(R.id.btnPrint).isEnabled = true
        } else {
            findViewById<Button>(R.id.btnPrint).isEnabled = false
        }
    }

    override fun selectFileButtonOnClick() {

    }


}