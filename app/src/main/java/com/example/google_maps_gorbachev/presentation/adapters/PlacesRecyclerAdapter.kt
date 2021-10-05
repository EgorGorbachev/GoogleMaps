package com.example.google_maps_gorbachev.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.google_maps_gorbachev.Models.Loc
import com.example.google_maps_gorbachev.databinding.LocItemBinding


class PlacesRecyclerAdapter(
	private var listener: OnItemClick
) : ListAdapter<Loc, PlacesRecyclerAdapter.MyViewHolder>(LOCATION_COMPARATOR) {
	
	inner class MyViewHolder(private var binding: LocItemBinding) :
		RecyclerView.ViewHolder(binding.root) {
		
		init {
			binding.root.setOnClickListener {
				val position = bindingAdapterPosition
				if (position != RecyclerView.NO_POSITION) {
					val item = getItem(position)
					if (item != null) {
						listener.onItemClick(item)
					}
				}
			}
		}
		
		fun bind(loc: Loc) {
			binding.apply {
				locItemAddress.text = loc.locName
			}
		}
	}
	
	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): MyViewHolder {
		val binding =
			LocItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		return MyViewHolder(binding)
	}
	
	override fun onBindViewHolder(
		holder: MyViewHolder,
		position: Int
	) {
		val currentItem = getItem(position)
		
		if (currentItem != null) {
			holder.bind(currentItem)
		}
	}
	
	interface OnItemClick {
		fun onItemClick(loc: Loc)
	}
	
	companion object {
		private val LOCATION_COMPARATOR = object : DiffUtil.ItemCallback<Loc>() {
			override fun areItemsTheSame(
				oldItem: Loc,
				newItem: Loc
			) = oldItem.id == newItem.id
			
			override fun areContentsTheSame(
				oldItem: Loc,
				newItem: Loc
			) = oldItem == newItem
		}
	}
}