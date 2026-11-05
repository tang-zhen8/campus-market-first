package com.tangzhen.campusmarket.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.FragmentMessageBinding

/**
 * 消息页：汇总所有商品下的留言（自选功能的占位与雏形）
 * 后续可扩展为买卖双方私聊（IM）
 */
class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private val adapter = MessageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMessages.adapter = adapter

        val messages = MarketRepository.getCommentMessages()
        adapter.submitList(messages)
        binding.emptyView.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
