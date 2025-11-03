package com.shreefinance.activity.ui.applyfroloan

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import com.shreefinance.activity.DashboardActivity
import com.shreefinance.activity.LoginActivity
import com.shreefinance.api.BrandItem
import com.shreefinance.api.BrandResponse
import com.shreefinance.api.LoanIdResponse
import com.shreefinance.api.ProductTypeItem
import com.shreefinance.api.ProductTypeResponse
import com.shreefinance.api.ReferenceItem
import com.shreefinance.api.ReferenceResponse
import com.shreefinance.api.RetrofitClient
import com.shreefinance.api.hideLoading
import com.shreefinance.api.showLoading
import com.shreefinance.databinding.FragmentApplyForLoanBinding
import com.shreefinance.ui.PrefsHelper.clearAll
import com.shreefinance.ui.PrefsHelper.getAccessToken
import com.shreefinance.utils.CustomAlertDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ApplyForLoanFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var parentActivity: DashboardActivity? = null
    private lateinit var binding: FragmentApplyForLoanBinding
    private var productTypeid: Int=0
    private var brandid: Int=0
    private var refid: Int=0
    private var pendingPermissionRequest: String? = null
    private var takephottype=""
    private var aadharFrontUri: Uri?=null
    private var aadharBackUri: Uri?=null
    private var panUri: Uri?=null
    private var photoUri: Uri?=null
    private var loanType: String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentActivity = activity as? DashboardActivity
        binding = FragmentApplyForLoanBinding.inflate(inflater, container, false)
        getReferences()
        getProductType()

        loadData()
        listener()
       // etspLoanType,etInvoiceAmount
        return binding.root
    }


    private fun calculateDownPaymentDifference() {
        val totalText = binding.etTotalDownPayment.text.toString().trim()
        val givenText = binding.etGivenDownPayment.text.toString().trim()

        val total = totalText.toDoubleOrNull() ?: 0.0
        val given = givenText.toDoubleOrNull() ?: 0.0

        val remaining = total - given

        // Example: show result in a TextView
        binding.etPendingDownPayment.text = remaining.toString()

        // Or log it
        Log.d("DownPayment", "Remaining: $remaining")
    }
    private fun getBrands(productTypeid: Int) {
        val token = "Bearer " + getAccessToken(requireContext())
        RetrofitClient.api.getBrands(token,productTypeid)
            .enqueue(object : Callback<BrandResponse> {
                override fun onResponse(call: Call<BrandResponse>, response: Response<BrandResponse>) {
                    if (response.isSuccessful) {
                        val brandList = response.body()?.data ?: emptyList()

                        if (brandList.isNotEmpty()) {
                            setupBrandSpinner(brandList)
                        } else {
                            Toast.makeText(requireContext(), "No brands found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BrandResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }
    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun listener()
    {
        binding.etTotalDownPayment.addTextChangedListener {
            calculateDownPaymentDifference()
        }

        binding.etGivenDownPayment.addTextChangedListener {
            calculateDownPaymentDifference()
        }

        binding.etNumberOfEMI.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Called before text changes (optional)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Called as the text is changing
                val enteredText = s.toString()
                Log.d("EMI", "User entered: $enteredText")

                // Example: Perform some logic
                if (enteredText.isNotEmpty()) {
                    val emiValue = enteredText.toIntOrNull() ?: 0
                    calculateEMI()
                    // Do something with emiValue
                }
            }


        })

    }
    private fun calculateEMI() {
        val context = requireContext() // use `this` if inside an Activity

        val invoiceText = binding.etInvoiceAmount.text.toString().trim()
        val processingText = binding.etIProcessingFees.text.toString().trim()
        val downPaymentText = binding.etGivenDownPayment.text.toString().trim()
        val emiText = binding.etNumberOfEMI.text.toString().trim()

        // Validation checks
        if (invoiceText.isEmpty()) {
            binding.etInvoiceAmount.error = "Enter invoice amount"
            Toast.makeText(context, "Please enter invoice amount", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }
        if (processingText.isEmpty()) {
            binding.etIProcessingFees.error = "Enter processing fee"
            Toast.makeText(context, "Please enter processing fee", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }
        if (downPaymentText.isEmpty()) {
            binding.etGivenDownPayment.error = "Enter down payment amount"
            Toast.makeText(context, "Please enter down payment amount", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }
        if (emiText.isEmpty()) {
            binding.etNumberOfEMI.error = "Enter number of EMI"
            Toast.makeText(context, "Please enter number of EMI", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }

        val invoiceAmount = invoiceText.toDoubleOrNull() ?: 0.0
        val processingFee = processingText.toDoubleOrNull() ?: 0.0
        val downPayment = downPaymentText.toDoubleOrNull() ?: 0.0
        val numberOfEMI = emiText.toIntOrNull() ?: 0

        // Additional numeric validation
        if (invoiceAmount <= 0) {
            binding.etInvoiceAmount.error = "Invoice amount must be greater than 0"
            Toast.makeText(context, "Invoice amount must be greater than 0", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }
        if (numberOfEMI <= 0) {
            binding.etNumberOfEMI.error = "Number of EMI must be greater than 0"
            Toast.makeText(context, "Number of EMI must be greater than 0", Toast.LENGTH_SHORT).show()
            binding.etEMIAmount.setText("")
            binding.etNumberOfEMI.setText("")
            return
        }

        // Calculate EMI
        val emiAmount = (invoiceAmount + processingFee - downPayment) / numberOfEMI
        binding.etEMIAmount.setText(String.format("%.2f", emiAmount))
        Toast.makeText(context, "EMI calculated successfully", Toast.LENGTH_SHORT).show()
    }



    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = saveBitmapToCache(it, context = requireContext())
                val filePart = createFilePartFromUri(uri, context = requireContext())

                if(takephottype=="111")
                {
                    binding.etAadharFront.setText(uri.lastPathSegment)
                    aadharFrontUri=uri
                } else if(takephottype=="222")
                {aadharBackUri=uri
                    binding.etAadharBack.setText(uri.lastPathSegment)
                } else if(takephottype=="333")
                {
                    panUri=uri
                    binding.etPanImage.setText(uri.lastPathSegment)
                } else if(takephottype=="4444")
                {
                    photoUri=uri
                    binding.etCustomerPhoto.setText(uri.lastPathSegment)
                }

              //  viewModel.uploadAttachment(filePart, docname = docname)
            }
        }


    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val filePart = createFilePartFromUri(uri, context = requireContext())

                val fileName = getFileNameFromUri(requireContext(), uri)

                if(takephottype=="111")
                {
                    binding.etAadharFront.setText(uri.lastPathSegment)
                    aadharFrontUri=uri
                } else if(takephottype=="222")
                {aadharBackUri=uri
                    binding.etAadharBack.setText(uri.lastPathSegment)
                } else if(takephottype=="333")
                {
                    panUri=uri
                    binding.etPanImage.setText(uri.lastPathSegment)
                } else if(takephottype=="444")
                {
                    photoUri=uri
                    binding.etCustomerPhoto.setText(uri.lastPathSegment)
                }

            }
        }

   /* private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }*/

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when (pendingPermissionRequest) {
                "camera" -> {
                    if (permissions[Manifest.permission.CAMERA] == true) {
                        openCamera()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Camera permission denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "gallery" -> {
                    val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
                    } else {
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                    }
                    if (granted) {
                        openGallery()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gallery permission denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    private fun setupBrandSpinner(brandList: List<BrandItem>) {
        val spinnerItems = mutableListOf("Select Brand")
        spinnerItems.addAll(brandList.map { it.brand_name })

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spBrandName.adapter = adapter

        binding.spBrandName.setSelection(0)

        binding.spBrandName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedBrand = brandList[position-1]
                    brandid=selectedBrand.id
                    binding.etspBrandName.setText(selectedBrand.brand_name)
                    Toast.makeText(requireContext(), "Selected: ${selectedBrand.brand_name}", Toast.LENGTH_SHORT).show()

                    // You can use selectedBrand.id for further API calls
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getProductType() {
        val token = "Bearer " + getAccessToken(requireContext())

        RetrofitClient.api.getProductTypes(token).enqueue(object : Callback<ProductTypeResponse> {
            override fun onResponse(
                call: Call<ProductTypeResponse>,
                response: Response<ProductTypeResponse>
            ) {
                if (response.isSuccessful) {
                    val typeList = response.body()?.data ?: emptyList()
                    setupSpinnerForProduct(typeList)
                } else {
                    Log.e("API", "Error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ProductTypeResponse>, t: Throwable) {
                Log.e("API", "Failure: ${t.message}")
            }
        })
    }

    private fun setupSpinnerForProduct(typeList: List<ProductTypeItem>) {
        val names = typeList.map { it.type }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spProductType.adapter = adapter

        binding.spProductType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = typeList[position]
                productTypeid=selected.id
                getBrands(productTypeid)
                binding.etspProductType.setText(selected.type)
                Log.d("Selected Product", "ID: ${selected.id}, Type: ${selected.type}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getReferences() {
        val token = "Bearer " + getAccessToken(requireContext())

        RetrofitClient.api.getReferences(token).enqueue(object : Callback<ReferenceResponse> {
            override fun onResponse(
                call: Call<ReferenceResponse>,
                response: Response<ReferenceResponse>
            ) {
                if (response.isSuccessful) {
                    val referenceList = response.body()?.data ?: emptyList()

                    val spinnerList = mutableListOf("Select Reference")
                    spinnerList.addAll(referenceList.map { "${it.name} - ${it.mobile_number}" })
                    setupSpinner(referenceList)
                } else {
                    Log.e("API", "Error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ReferenceResponse>, t: Throwable) {
                Log.e("API", "Failure: ${t.message}")
            }
        })
    }
    private fun setupSpinner(referenceList: List<ReferenceItem>) {

        // Combine name + mobile number for spinner display
        val displayList = referenceList.map { "${it.name} - ${it.mobile_number}" }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, displayList)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spReference.adapter = adapter

        var isSpinnerInitialized = false

        binding.spReference.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = referenceList[position]
                Log.d("Selected", "ID: ${selected.id}, Name: ${selected.name}, Mobile: ${selected.mobile_number}")

                // Skip the first auto-triggered selection
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }

                refid=selected.id
                // Set name or mobile in EditTexts
                binding.etspLoanReferencePerson.setText(selected.name +"(${selected.mobile_number})")
                //binding.etMobileNumber.setText(selected.mobile_number)

                // You can also use selected.id for another API call
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun loadData() {
       /// val loanTypes = listOf("Clean Credit", "EMI")
        val loanTypeValues = listOf(0, 1)

       // val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, loanTypes)
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
       // binding.spLoanType.adapter = adapter
        // Handle EditText click
        binding.etRegDate.setOnClickListener {
            openCalendar( binding.etRegDate,"regdate")
        }
        binding.etDownPaymentDueDate.setOnClickListener {
            openCalendar(binding.etDownPaymentDueDate,"downpayment")
        }
        binding.etEMIStartDate.setOnClickListener {
            openCalendar(binding.etEMIStartDate,"EMIStartDate")
        }
        binding.etEMIEndDate.setOnClickListener {
            openCalendar(binding.etEMIEndDate,"EMIEndDate")
        }

        binding.etspLoanType.setOnClickListener {
            binding.spLoanType.visibility = View.INVISIBLE
            binding.spLoanType.performClick()
        }
        binding.etspLoanReferencePerson.setOnClickListener {
            binding.spReference.visibility = View.INVISIBLE
            binding.spReference.performClick()
        }
        binding.etspProductType.setOnClickListener {
            binding.spProductType.visibility = View.INVISIBLE
            binding.spProductType.performClick()
        }
  binding.etspBrandName.setOnClickListener {
            binding.spBrandName.visibility = View.INVISIBLE
            binding.spBrandName.performClick()
        }
        binding.btnSubmit.setOnClickListener {
            submit()
        }

        binding.etAadharFront.setOnClickListener {
            CustomAlertDialog.showSelectionOptionDialog(
                requireContext(),
                onCameraClick = {
                    pendingPermissionRequest = "camera"
                    takephottype="111"
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                },
                onGalleryClick = {
                    takephottype="111"
                    pendingPermissionRequest = "gallery"
                    val galleryPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    if (ContextCompat.checkSelfPermission(requireContext(), galleryPermission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(galleryPermission))
                    } else {
                        openGallery()
                    }
                }, onDocumentClick = {

                }
            )
        }

        binding.etAadharBack.setOnClickListener {
            CustomAlertDialog.showSelectionOptionDialog(
                requireContext(),
                onCameraClick = {
                    takephottype="222"
                    pendingPermissionRequest = "camera"
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                },
                onGalleryClick = {
                    takephottype="222"
                    pendingPermissionRequest = "gallery"
                    val galleryPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    if (ContextCompat.checkSelfPermission(requireContext(), galleryPermission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(galleryPermission))
                    } else {
                        openGallery()
                    }
                }, onDocumentClick = {

                }
            )
        }

        binding.etPanImage.setOnClickListener {
            CustomAlertDialog.showSelectionOptionDialog(
                requireContext(),
                onCameraClick = {
                    takephottype="333"
                    pendingPermissionRequest = "camera"
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                },
                onGalleryClick = {
                    takephottype="333"
                    pendingPermissionRequest = "gallery"
                    val galleryPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    if (ContextCompat.checkSelfPermission(requireContext(), galleryPermission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(galleryPermission))
                    } else {
                        openGallery()
                    }
                }, onDocumentClick = {

                }
            )
        }

        binding.etCustomerPhoto.setOnClickListener {
            CustomAlertDialog.showSelectionOptionDialog(
                requireContext(),
                onCameraClick = {
                    takephottype="4444"
                    pendingPermissionRequest = "camera"
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                },
                onGalleryClick = {
                    takephottype="444"
                    pendingPermissionRequest = "gallery"
                    val galleryPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    if (ContextCompat.checkSelfPermission(requireContext(), galleryPermission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(arrayOf(galleryPermission))
                    } else {
                        openGallery()
                    }
                }, onDocumentClick = {

                }
            )
        }

        val loanTypes = listOf("Select Loan Type","Clean Credit", "EMI")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, loanTypes)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spLoanType.adapter = adapter

        var isSpinnerInitialized = false

        binding.spLoanType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                Log.e("Hello","Hello"+position)
                // Ignore first automatic call when spinner initializes
                //binding.spLoanType.visibility = View.GONE
                /*if (position == 0) {
                    binding.etspLoanType.text = ""
                    return
                }*/
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }

                val selectedText = loanTypes[position]
                binding.etspLoanType.text = selectedText
                binding.spLoanType.visibility = View.GONE

                Log.e("ApplyForLoanFragment", "Selected: $selectedText")

                // 🔹 Call API AFTER user selects option
                if (selectedText == "EMI") {
                    getLoanId(1)
                    loanType="1"
                    binding.etNumberOfEMI.visibility=View.VISIBLE
                    binding.etEMIAmount.visibility=View.VISIBLE
                    binding.etEMIEndDate.isFocusable = false
                    binding.etEMIEndDate.isFocusableInTouchMode = false
                } else if (selectedText == "Clean Credit"){
                    getLoanId(0)
                    loanType="0"
                    binding.etNumberOfEMI.visibility=View.GONE
                    binding.etEMIAmount.visibility=View.GONE
                    binding.etEMIEndDate.isFocusable = true
                    binding.etEMIEndDate.isFocusableInTouchMode = true
                }
                else
                {
                    binding.etLoanId.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.etspLoanType.text = ""
                Log.e("Hello","Hello")
            }

        }


    }

    private fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun createFilePartFromUri(fieldName: String, uri: Uri?, context: Context): MultipartBody.Part? {
        if (uri == null) return null

        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, getFileNameFromUri(context, uri))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val requestFile = file.asRequestBody(type?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "file"
    }
    private fun submit() {
        val loanType= binding.etspLoanType.text.toString()
        val loanIddata= binding.etLoanId.text.toString()
        val amountdata= binding.etInvoiceAmount.text.toString()
        val processingFees= binding.etIProcessingFees.text.toString()
        val loanReferencePerson= binding.etspLoanReferencePerson.text.toString()
        val productTypedata= binding.etspProductType.text.toString()
        val modelname= binding.etModelname.text.toString()
        val IMEInumber= binding.etIMEInumber.text.toString()
        val totalDownPaymentdata= binding.etTotalDownPayment.text.toString()
        val givenDownPaymentdata= binding.etGivenDownPayment.text.toString()
        val pendingDownPayment= binding.etPendingDownPayment.text.toString()
        val downPaymentDueDate= binding.etDownPaymentDueDate.text.toString()
        Log.e("ApplyForLoanFragment", "Selected: $downPaymentDueDate")
        val EMIStartDate= binding.etEMIStartDate.text.toString()
        val EMIEndDate= binding.etEMIEndDate.text.toString()
        val firstNamedata= binding.etFirstName.text.toString()
        val contactNumber= binding.etContactNumber.text.toString()
        val alternateNumber= binding.etAlternateNumber.text.toString()
        val citydata= binding.etCity.text.toString()
        val aadhar= binding.etAadhar.text.toString()
        val pan= binding.etPan.text.toString()
        val etRegDate= binding.etRegDate.text.toString()
        val EMIAmount= binding.etEMIAmount.text.toString()
        val numberOfEMI= binding.etNumberOfEMI.text.toString()


        when {
            binding.etspLoanType.text.isNullOrBlank() -> showError("Please select loan type")
            binding.etInvoiceAmount.text.isNullOrBlank() -> showError("Please enter invoice amount")
            binding.etRegDate.text.isNullOrBlank() -> showError("Please select registration Date")

            binding.etspProductType.text.isNullOrBlank() -> showError("Please select product type")
            binding.etspBrandName.text.isNullOrBlank() -> showError("Please select Brand Name")
            binding.etModelname.text.isNullOrBlank() -> showError("Please enter model name")

            loanType == "1" && binding.etIMEInumber.text.isNullOrBlank() ->
                showError("Please enter IMEI number ")
            loanType == "1" && binding.etEMIAmount.text.isNullOrBlank() ->
                showError("Please enter Amount number")

            binding.etIMEInumber.text.isNullOrBlank() -> showError("Please enter IMEI number")
            binding.etEMIStartDate.text.isNullOrBlank() -> showError("Please select EMI start date")
            binding.etEMIEndDate.text.isNullOrBlank() -> showError("Please select EMI end date")
            binding.etFirstName.text.isNullOrBlank() -> showError("Please enter first name")
            binding.etContactNumber.text.isNullOrBlank() -> showError("Please enter contact number")

            else -> {
                // All fields are filled, proceed with submission
            }
        }
        var selectedLoanId=-9
        if(loanType=="Clean Credit")
        {
            selectedLoanId=0;
        }
        else
        {
            selectedLoanId=1;
        }
        showLoading(requireActivity())
        // Text fields
        val loanId = createPartFromString(loanIddata)
        val agentId = createPartFromString("9")
        val refId = createPartFromString(refid.toString())
        val firstName = createPartFromString(firstNamedata)
        val productType = createPartFromString(productTypeid.toString())
        val brandId = createPartFromString(brandid.toString())
        val modelName = createPartFromString(modelname)
        val imeiNumber = createPartFromString(IMEInumber)
        val amount = createPartFromString(amountdata)
        val processingfees = createPartFromString(processingFees)
        val city = createPartFromString(citydata)
        val regDate = createPartFromString(etRegDate)
        val mobileNumber = createPartFromString(contactNumber)
        val noOfEmi = createPartFromString(numberOfEMI)
        val emiAmount = createPartFromString(EMIAmount)
        val emiStartDate = createPartFromString(EMIStartDate)
        val downpaymentduedate = createPartFromString(downPaymentDueDate)
        val givenDownPayment = createPartFromString(givenDownPaymentdata)
        val totalDownPayment = createPartFromString(totalDownPaymentdata)
        val loantype = createPartFromString(selectedLoanId.toString())
        val aadharcardnumber = createPartFromString(aadhar)
        val pancardnumber = createPartFromString(pan)
        val pendingdownpayment = createPartFromString(pendingDownPayment)
        val alternatenum = createPartFromString(alternateNumber)
        val EMIEnddate = createPartFromString(EMIEndDate)


        // File parts (URIs from file picker or camera)
        val aadharFrontPart = createFilePartFromUri("aadhar_card_front", aadharFrontUri, requireActivity())
        val aadharBackPart = createFilePartFromUri("aadhar_card_back", aadharBackUri, requireActivity())
        val panPart = createFilePartFromUri("pan_card_image", panUri, requireActivity())
        val userPhotoPart = createFilePartFromUri("user_photo", photoUri, requireActivity())

/*
        val call =  RetrofitClient.api.applyLoan(
            token = "Bearer " + getAccessToken(requireContext()), loantype, // ✅ Pass Authorization header
            loanId, agentId, refId, firstName, productType, brandId, modelName, imeiNumber,
            amount, mobileNumber, noOfEmi, emiAmount, regDate, emiStartDate, EMIEnddate ,processingfees,
            givenDownPayment, totalDownPayment,aadharcardnumber,
            pancardnumber,pendingdownpayment,
            processingfees,city,
            aadharFrontPart, aadharBackPart, panPart, userPhotoPart
        )
*/
        val call = RetrofitClient.api.applyLoan(
            token = "Bearer " + getAccessToken(requireContext()), // ✅ Authorization header
            loantype = loantype,
            loanId = loanId,
            agentId = agentId,
            refId = refId,
            firstName = firstName,
            productType = productType,
            brandId = brandId,
            modelName = modelName,
            imeiNumber = imeiNumber,
            amount = amount,
            mobileNumber = mobileNumber,
            noOfEmi = noOfEmi,
            emiAmount = emiAmount,
            regDate = regDate,
            emiStartDate = emiStartDate,
            emienddate = EMIEnddate,
            processingfees = processingfees,
            givenDownPayment = givenDownPayment,
            totalDownPayment = totalDownPayment,
            aadharcardnumber = aadharcardnumber,
            pancardnumber = pancardnumber,
            pendingdownpayment = pendingdownpayment,
            city = city,
            aadharCardFront = aadharFrontPart,
            aadharCardBack = aadharBackPart,
            panCardImage = panPart,
            userPhoto = userPhotoPart,
            downpaymentduedate = downpaymentduedate,
            alternatenumber=alternatenum
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
               hideLoading()
                if(response.code() == 401) {
                    Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()

                    clearAll(requireActivity())

                    // Navigate to Login screen
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                if (response.isSuccessful) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Your Apply Loan applauded successfully!")
                        .setIcon(R.drawable.checkbox_on_background)
                        .setPositiveButton("OK") { dialog, _ ->
                            requireActivity().onBackPressed()
                            dialog.dismiss()
                        }
                        .show()
                    Log.d("ApplyLoan", "✅ Success: ${response.body()?.string()}")
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage(response.errorBody()?.string())
                        .setIcon(R.drawable.checkbox_on_background)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    Log.e("ApplyLoan", "❌ Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ApplyLoan", "⚠️ Failed: ${t.message}")
                hideLoading()
            }
        })
    }


    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun openCalendar(etRegDate: TextView,type:String) {
        // Get today’s date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                // Format to yyyy-MM-dd
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = formatter.format(selectedDate.time)
                if(loanType=="1" && type=="EMIStartDate")
                {
                    if (binding.etNumberOfEMI.text.isNotEmpty()) {
                        val emiCount = binding.etNumberOfEMI.text.toString().toInt()
                        selectedDate.add(Calendar.MONTH, emiCount) // Add EMI months
                    }

                    // Format to yyyy-MM-dd
                    val formatterr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDatee = formatterr.format(selectedDate.time)
                    //etRegDate.text = formattedDatee
                    etRegDate.text = formattedDate
                    binding.etEMIEndDate.setText(formattedDatee)
                    binding.etEMIEndDate.isFocusable = false
                    binding.etEMIEndDate.isClickable = false
                    binding.etEMIEndDate.isFocusableInTouchMode = false
                    binding.etEMIEndDate.setOnClickListener {
                    }
                }
                else
                {
                    etRegDate.text = formattedDate
                }


                // Set date to TextView

            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
    private fun getLoanId(id: Int) {
        showLoading(requireActivity())

        val token = "Bearer " + getAccessToken(requireActivity())
        val call = RetrofitClient.api.getLoanId(token, id)

        call.enqueue(object : Callback<LoanIdResponse> {
            override fun onResponse(
                call: Call<LoanIdResponse>,
                response: Response<LoanIdResponse>
            ) {
                hideLoading()

                when {
                    response.code() == 401 -> {
                        // 🔹 Token expired or unauthorized — navigate to Login
                        Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()

                        clearAll(requireActivity())

                        // Navigate to Login screen
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    response.isSuccessful -> {
                        val loanResponse = response.body()
                        if (loanResponse != null && loanResponse.status) {
                            val loanId = loanResponse.data
                            println("Loan ID: $loanId")
                            binding.etLoanId.setText(loanId)
                        } else {
                            println("Failed: ${loanResponse?.message}")
                        }
                    }

                    else -> {
                        println("Request failed: ${response.code()} - ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<LoanIdResponse>, t: Throwable) {
                hideLoading()
                println("Error: ${t.message}")
            }
        })
    }

    /*private fun getLoanId(id: Int) {
        showLoading(requireActivity())

        val token = "Bearer " + getAccessToken(requireActivity())
        val call = RetrofitClient.api.getLoanId(token, id) // loan_type = 1

        call.enqueue(object : retrofit2.Callback<LoanIdResponse> {
            override fun onResponse(
                call: Call<LoanIdResponse>,
                response: Response<LoanIdResponse>
            ) {
                hideLoading()
                if (response.isSuccessful) {
                    val loanResponse = response.body()
                    if (loanResponse != null && loanResponse.status) {
                        val loanId = loanResponse.data
                        println("Loan ID: $loanId")
                        binding. etLoanId.setText(loanId)
                        // 🔹 You can use it anywhere, e.g. set to TextView
                        // tvLoanId.text = loanId
                    } else {
                        println("Failed: ${loanResponse?.message}")
                    }
                } else {
                    println("Request failed: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoanIdResponse>, t: Throwable) {
                hideLoading()
                println("Error: ${t.message}")
            }
        })
    }*/

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String?, param2: String?) =
            ApplyForLoanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    /*fun saveBitmapToCache(bitmap: Bitmap,context: Context): Uri {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return Uri.fromFile(file)
    }*/
    fun saveBitmapToCache(bitmap: Bitmap, context: Context): Uri {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        // Use FileProvider to get a content URI
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // defined in manifest
            file
        )
    }
    fun createFilePartFromUri(uri: Uri, context: Context): MultipartBody.Part {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }
}
