package com.bignerdranch.android.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeDetailBinding
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

private const val DATE_FORMAT = "EEE, MMM, dd"
private const val TAG = "Crime Detail Fragment"

class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }

    private var photoUri: Uri = Uri.EMPTY
    private var photoName: String? = null

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            val photoFile = File(requireContext().applicationContext.filesDir, photoName!!)
            val bitmap = getRotatedBitmap(photoFile)
            crimeDetailViewModel.updateCrime { oldCrime ->
                when(crimeDetailViewModel.photoFileNameIndex) {
                    0 -> {
                        if(oldCrime.isFace){
                            detectFaces(requireContext(), photoName!!, binding.crimePhoto, bitmap) { numberOfDetectedFaces ->
                                Log.v(TAG, "$numberOfDetectedFaces faces detected")
                                oldCrime.copy(numFacesDetected = "$numberOfDetectedFaces face(s) detected")
                                binding.facesDetected.text = "$numberOfDetectedFaces face(s) detected"
                            }
                        }
                        else if(oldCrime.isMesh){
                            detectFaceMesh(requireContext(), photoName!!,bitmap, binding.crimePhoto)
                        }
                        else if(oldCrime.isContour) {
                            contour(requireContext(), photoName!!, bitmap, binding.crimePhoto)
                        }
                        else if(oldCrime.isSelfie){
                            segment(requireContext(), photoName!!, binding.crimePhoto, bitmap)
                        }
                        else {
                            context?.applicationContext?.filesDir?.let { filesDir ->
                                val fileOut = File(filesDir, photoName)
                                var fOut = FileOutputStream(fileOut)

                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)

                                fOut.flush()
                                fOut.close()
                            }
                        }
                        oldCrime.copy(photoFileName = photoName)
                    }
                    1 -> {
                        if(oldCrime.isFace){
                            detectFaces(requireContext(), photoName!!, binding.image2, bitmap){ numberOfDetectedFaces ->
                                Log.v(TAG, "$numberOfDetectedFaces faces detected")
                                oldCrime.copy(numFacesDetected = "$numberOfDetectedFaces face(s) detected")
                                binding.facesDetected.text = "$numberOfDetectedFaces face(s) detected"
                            }
                        }
                        else if(oldCrime.isMesh){
                            detectFaceMesh(requireContext(), photoName!!, bitmap, binding.image2)
                        }
                        else if(oldCrime.isContour) {
                            contour(requireContext(), photoName!!, bitmap, binding.image2)
                        }
                        else if(oldCrime.isSelfie){
                            segment(requireContext(), photoName!!, binding.image2, bitmap)
                        }
                        else {
                            context?.applicationContext?.filesDir?.let { filesDir ->
                                val fileOut = File(filesDir, photoName)
                                var fOut = FileOutputStream(fileOut)

                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)

                                fOut.flush()
                                fOut.close()
                            }
                        }
                        oldCrime.copy(photoFileName2 = photoName)
                    }
                    2 -> {
                        if(oldCrime.isFace){
                            detectFaces(requireContext(), photoName!!, binding.image3, bitmap){ numberOfDetectedFaces ->
                                Log.v(TAG, "$numberOfDetectedFaces faces detected")
                                oldCrime.copy(numFacesDetected = "$numberOfDetectedFaces face(s) detected")
                                binding.facesDetected.text = "$numberOfDetectedFaces face(s) detected"
                            }
                        }
                        else if(oldCrime.isMesh){
                            detectFaceMesh(requireContext(), photoName!!, bitmap, binding.image3)
                        }
                        else if(oldCrime.isContour) {
                            contour(requireContext(), photoName!!, bitmap, binding.image3)
                        }
                        else if(oldCrime.isSelfie){
                            segment(requireContext(), photoName!!, binding.image3, bitmap)
                        }
                        else {
                            context?.applicationContext?.filesDir?.let { filesDir ->
                                val fileOut = File(filesDir, photoName)
                                var fOut = FileOutputStream(fileOut)

                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)

                                fOut.flush()
                                fOut.close()
                            }
                        }
                        oldCrime.copy(photoFileName3 = photoName)
                    }
                    else -> {
                        if(oldCrime.isFace){
                            detectFaces(requireContext(), photoName!!, binding.image4, bitmap){ numberOfDetectedFaces ->
                                Log.v(TAG, "$numberOfDetectedFaces faces detected")
                                oldCrime.copy(numFacesDetected = "$numberOfDetectedFaces face(s) detected")
                                binding.facesDetected.text = "$numberOfDetectedFaces face(s) detected"
                            }
                        }
                        else if(oldCrime.isMesh){
                            detectFaceMesh(requireContext(), photoName!!, bitmap, binding.image4)
                        }
                        else if(oldCrime.isContour) {
                            contour(requireContext(), photoName!!, bitmap, binding.image4)
                        }
                        else if(oldCrime.isSelfie){
                            segment(requireContext(), photoName!!, binding.image4, bitmap)
                        }
                        else {
                            context?.applicationContext?.filesDir?.let { filesDir ->
                                val fileOut = File(filesDir, photoName)
                                var fOut = FileOutputStream(fileOut)

                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)

                                fOut.flush()
                                fOut.close()
                            }
                        }
                        oldCrime.copy(photoFileName4 = photoName)
                    }
                }
            }
            crimeDetailViewModel.incrementImgIdx()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentCrimeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            enableFaceDetection.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isFace = isChecked)
                }
                binding.enableMeshDetection.isChecked = false
                binding.enableSelfieSeg.isChecked = false
                binding.enableContourDetection.isChecked = false
            }

            enableMeshDetection.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isMesh = isChecked)
                }
                binding.enableSelfieSeg.isChecked = false
                binding.enableContourDetection.isChecked = false
                binding.enableFaceDetection.isChecked = false
            }

            enableContourDetection.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isContour = isChecked)
                }
                binding.enableSelfieSeg.isChecked = false
                binding.enableMeshDetection.isChecked = false
                binding.enableFaceDetection.isChecked = false
            }

            enableSelfieSeg.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSelfie = isChecked)
                }
                binding.enableMeshDetection.isChecked = false
                binding.enableContourDetection.isChecked = false
                binding.enableFaceDetection.isChecked = false
            }

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            val selectSuspectIntent = selectSuspect.contract.createIntent(
                requireContext(),
                null
            )
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)

            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date().toString().replace(" ","_").replace(":","_")}.JPG"
                Log.v(TAG, "photoName: $photoName")
                val photoFile = File(
                    requireContext().applicationContext.filesDir,
                    photoName
                )
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }


            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                null
            )
            crimeCamera.isEnabled = canResolveIntent(captureImageIntent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            crimeDate.text = crime.date.toString()
            crimeDate.setOnClickListener {
                findNavController().navigate(
                    CrimeDetailFragmentDirections.selectDate(crime.date)
                )
            }

            crimeSolved.isChecked = crime.isSolved
            enableFaceDetection.isChecked = crime.isFace
            enableMeshDetection.isChecked = crime.isMesh
            enableContourDetection.isChecked = crime.isContour
            enableSelfieSeg.isChecked = crime.isSelfie

            facesDetected.text = crime.numFacesDetected

            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }
                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }
            println("Current photoFileNameIndex: ${crimeDetailViewModel.photoFileNameIndex}")

            updatePhoto(crime.photoFileName, binding.crimePhoto)
            updatePhoto(crime.photoFileName2, binding.image2)
            updatePhoto(crime.photoFileName3, binding.image3)
            updatePhoto(crime.photoFileName4, binding.image4)

        }

    }

    private fun getCrimeReport(crime: Crime): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectText
        )
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun updatePhoto(photoFileName: String?, currentImageView: ImageView) {
        if (currentImageView.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                currentImageView.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    currentImageView.setImageBitmap(scaledBitmap)
                    currentImageView.tag = photoFileName
                    currentImageView.contentDescription =
                        getString(R.string.crime_photo_image_description)
                }
            } else {
                currentImageView.setImageBitmap(null)
                currentImageView.tag = null
                currentImageView.contentDescription =
                    getString(R.string.crime_photo_no_image_description)
            }
        }
    }
}
