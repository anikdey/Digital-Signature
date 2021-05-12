package com.anik.digitalsignature

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.anik.digitalsignature.databinding.ActivityMainBinding
import com.github.gcacace.signaturepad.views.SignaturePad
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListener()
        FileUtil.copyAsset(getContext(), assets)

        val tmpLocalFile = File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(), Constants.ORIGINAL_PDF_NAME)
        binding.pdfView.fromFile(tmpLocalFile).load()
    }

    private fun setUpListener() {

        binding.signDocumentButton.setOnClickListener {

            binding.pdfView.visibility = View.GONE
            binding.signDocumentButton.visibility = View.GONE

            binding.signaturePad.visibility = View.VISIBLE
            binding.buttonContainer.visibility = View.VISIBLE
        }

        binding.clearPad.setOnClickListener{ binding.signaturePad.clear() }

        binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {

            override fun onStartSigning() {}

            override fun onSigned() {
                binding.saveSignature.isEnabled = true
                binding.clearPad.isEnabled = true
            }

            override fun onClear() {
                binding.saveSignature.isEnabled = false
                binding.clearPad.isEnabled = false
            }

        })

        binding.saveSignature.setOnClickListener {
            try {
                FileUtil.saveSignature(binding.signaturePad.signatureBitmap, getContext())?.let { signaturePath->
                    FileUtil.generateSignedPdf(signaturePath, getContext())?.let { generatedPdfPath->
                        loadPreviewActivity(generatedPdfPath)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.fromGallery.setOnClickListener {
            val path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/"+Constants.SAVED_SIGNATURE_IMAGE_NAME
            if(isSignatureExists(path)) {
                showAlertDialog(path)
            } else {
                Toast.makeText(getContext(), "There is no previously saved signature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlertDialog(path: String) {
        val builder = AlertDialog.Builder(this)
        var view = layoutInflater.inflate(R.layout.saved_signature_alert, null)
        var imageView = view.findViewById<AppCompatImageView>(R.id.savedSignatureImageView)
        val bitmap = BitmapFactory.decodeFile(path)
        imageView.setImageBitmap(bitmap)
        builder.setView(view)
        builder.apply {
            setPositiveButton("Ok") { _, _ ->
                FileUtil.generateSignedPdf(path, getContext())?.let {
                    loadPreviewActivity(it)
                }
            }
            setNegativeButton("Cancel") { _, _ ->

            }
        }
        builder.show()
    }

    private fun isSignatureExists(path: String): Boolean {
        return File(path).exists()
    }

    private fun getContext(): Context {
        return this
    }

    private fun loadPreviewActivity(generatedPdfPath: String) {
        startActivity(PreviewActivity.newIntent(getContext(), generatedPdfPath))
    }
}