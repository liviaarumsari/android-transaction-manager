package com.example.abe.ui.graph

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.abe.ABEApplication
import com.example.abe.R
import com.example.abe.databinding.FragmentGraphBinding
import com.example.abe.domain.FormatCurrencyUseCase
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor


class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels {
        GraphViewModelFactory((activity?.application as ABEApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        drawGraph()
        return binding.root
    }

    private fun drawGraph() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        val user = sharedPref.getString("user", "").toString()
        val expenses = viewModel.getExpenses(user).toDouble()
        val income = viewModel.getIncome(user).toDouble()

        withContext(Dispatchers.Main) {
            val total = expenses + income

            val expensePercentage = floor(expenses * 100 / total).toInt()
            val incomePercentage = 100 - expensePercentage

            val expensePtgStr = "${expensePercentage}%"
            val incomePtgStr = "${incomePercentage}%"

            binding.tvExpensePercentage.text = expensePtgStr
            binding.tvIncomePercentage.text = incomePtgStr

            val currencyFormatter = FormatCurrencyUseCase()
            binding.tvExpenseAmount.text = currencyFormatter(expenses.toInt())
            binding.tvIncomeAmount.text = currencyFormatter(income.toInt())

            if (total != 0.0) {
                binding.tvNoTransactions.visibility = View.GONE
                binding.chartPie.visibility = View.VISIBLE

                val entries = ArrayList<PieEntry>().apply {
                    if (expenses != 0.0)
                        add(PieEntry(expenses.toFloat(), "Expenses"))
                    if (income != 0.0)
                        add(PieEntry(income.toFloat(), "Income"))
                }

                val entryColors = ArrayList<Int>().apply {
                    if (expenses != 0.0)
                        add(requireContext().getColor(R.color.primary))
                    if (income != 0.0)
                        add(requireContext().getColor(R.color.secondary))
                }


                val dataSet = PieDataSet(entries, "Income & Expenses").apply {
                    colors = entryColors
                    setDrawValues(false)
                    xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    valueLinePart1OffsetPercentage = 100f
                    valueLinePart1Length = 0.8f
                    valueLinePart2Length = 0f
                }

                val data = PieData(dataSet)

                binding.chartPie.apply {
                    isDrawHoleEnabled = false
                    dragDecelerationFrictionCoef = 0.95f
                    rotationAngle = 0f
                    isRotationEnabled = false
                    isHighlightPerTapEnabled = true
                    legend.isEnabled = false
                    description.isEnabled = false
                    setExtraOffsets(20f, 15f, 20f, 15f)

                    setEntryLabelTextSize(14f)
                    setEntryLabelColor(Color.BLACK)

                    setData(data)
                    highlightValues(null)
                    invalidate()
                }
            } else {
                binding.tvNoTransactions.visibility = View.VISIBLE
                binding.chartPie.visibility = View.GONE
            }
        }
    }
}