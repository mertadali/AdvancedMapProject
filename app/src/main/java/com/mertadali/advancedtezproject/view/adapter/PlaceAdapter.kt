package com.mertadali.advancedtezproject.view.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mertadali.advancedtezproject.R
import com.mertadali.advancedtezproject.databinding.RecyclerRowBinding
import com.mertadali.advancedtezproject.view.model.Place
import com.mertadali.advancedtezproject.view.model.Post
import com.mertadali.advancedtezproject.view.view.MapsFragment
import com.mertadali.advancedtezproject.view.view.PostFragment
import com.squareup.picasso.Picasso

class PlaceAdapter(private val placeList: ArrayList<Place>) : RecyclerView.Adapter<PlaceAdapter.RowHolder>() {

    private var postList: List<Post> = emptyList()

    class RowHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(recyclerRowBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val place = placeList[position]
        holder.recyclerRowBinding.placeNameText.text = place.name
        holder.recyclerRowBinding.recyclerCommentText.text = place.description

        postList.let { posts ->
            if (position < posts.size) {
                holder.recyclerRowBinding.recyclerEmailText.text = posts[position].userEmail
                Picasso.get().load(posts[position].downloadUrl).into(holder.recyclerRowBinding.recyclerImageView)
            }
        }

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("selectedPlace", place)
                putString("info", "old")
            }

            val fragment = MapsFragment().apply {
                arguments = bundle
            }


            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }


    fun setPostList(posts: ArrayList<Post>) {
        postList = posts

    }
}
