package de.ph1b.audiobook.features.settings.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import de.ph1b.audiobook.R
import de.ph1b.audiobook.features.settings.SettingsSetListener
import de.ph1b.audiobook.injection.App
import de.ph1b.audiobook.misc.layoutInflater
import de.ph1b.audiobook.misc.onProgressChanged
import de.ph1b.audiobook.misc.value
import de.ph1b.audiobook.persistence.PrefsManager
import kotlinx.android.synthetic.main.dialog_amount_chooser.view.*
import javax.inject.Inject


class AutoRewindDialogFragment : DialogFragment() {

    @Inject lateinit var prefs: PrefsManager

    private lateinit var settingsSetListener: SettingsSetListener
    private lateinit var rewindText: TextView

    private val SEEK_BAR_MIN = 0
    private val SEEK_BAR_MAX = 20

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        settingsSetListener = context as SettingsSetListener
    }

    private fun setText(progress: Int) {
        val autoRewindSummary = context.resources.getQuantityString(R.plurals.pref_auto_rewind_summary, progress, progress)
        rewindText.text = autoRewindSummary
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        // view binding
        val v = context.layoutInflater().inflate(R.layout.dialog_amount_chooser, null)
        rewindText = v.textView

        val oldRewindAmount = prefs.autoRewindAmount.value()
        v.seekBar.max = SEEK_BAR_MAX - SEEK_BAR_MIN
        v.seekBar.progress = oldRewindAmount - SEEK_BAR_MIN
        v.seekBar.onProgressChanged {
            setText(it)
        }

        // text
        setText(v.seekBar.progress)

        return MaterialDialog.Builder(context)
                .title(R.string.pref_auto_rewind_title)
                .customView(v, true)
                .positiveText(R.string.dialog_confirm)
                .negativeText(R.string.dialog_cancel)
                .onPositive { materialDialog, dialogAction ->
                    val newRewindAmount = v.seekBar.progress + SEEK_BAR_MIN
                    prefs.autoRewindAmount.set(newRewindAmount)
                    settingsSetListener.onSettingsSet(oldRewindAmount != newRewindAmount)
                }
                .build()
    }

    companion object {
        val TAG: String = AutoRewindDialogFragment::class.java.simpleName
    }
}
