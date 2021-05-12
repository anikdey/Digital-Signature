package com.anik.digitalsignature

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anik.digitalsignature.databinding.ActivityPreviewBinding
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var path = intent.getStringExtra(EXTRA_GENERATED_PDF_PATH)
        var tmpLocalFile = File(path)
        binding.pdfView.fromFile(tmpLocalFile).load()
    }

    companion object {

        private const val EXTRA_GENERATED_PDF_PATH = "generated_pdf_path"

        @JvmStatic
        fun newIntent(context: Context, generatedPdfPath: String) : Intent {
            var intent = Intent(context, PreviewActivity::class.java)
            intent.putExtra(EXTRA_GENERATED_PDF_PATH, generatedPdfPath)
            return intent
        }

    }

}