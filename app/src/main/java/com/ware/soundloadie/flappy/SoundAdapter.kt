package com.ware.soundloadie.flappy

import androidx.annotation.LayoutRes
import com.shprot.easy.adapter.EasyAdapter
import com.shprot.easy.adapter.EasyViewHolder
class SoundAdapter (
    @LayoutRes
    itemLayoutRes: Int,
    itemCount: Int,
    binder: (EasyViewHolder) -> Unit = {}
) : EasyAdapter(itemLayoutRes,itemCount, binder)