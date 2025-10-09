/*
 * MIT License
 *
 * Copyright (c) 2020 Shreyas Patil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.shreyaspatil.foodium.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.foodium.BuildConfig
import dev.shreyaspatil.foodium.databinding.ActivityAboutBinding
import dev.shreyaspatil.foodium.ui.base.BaseActivity
import dev.shreyaspatil.foodium.utils.showToast

@AndroidEntryPoint
class AboutActivity : BaseActivity<AboutViewModel, ActivityAboutBinding>() {

    override val mViewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initView()
    }

    private fun initView() {
        val versionText =
            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE}"
        mViewBinding.textVersion.text = versionText

        mViewBinding.buttonRepo.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/PatilShreyas/Foodium")
            )
            startActivity(intent)
        }

        mViewBinding.buttonShare.setOnClickListener {
            val text =
                "Foodium – ${BuildConfig.VERSION_NAME}. Repo: " +
                    "https://github.com/PatilShreyas/Foodium"
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(share, null))
        }

        mViewBinding.buttonError.setOnClickListener {
            showToast("Simulated error")
        }
    }

    override fun getViewBinding(): ActivityAboutBinding =
        ActivityAboutBinding.inflate(layoutInflater)
}
