<template>
  <div v-if="visible" class="emoji-picker" @mousedown.prevent>
    <div class="picker-tabs">
      <button
        type="button"
        class="tab-btn"
        :class="{ active: activeTab === 'emoji' }"
        title="Emoji"
        @click="activeTab = 'emoji'"
      >
        😊
      </button>
      <button
        v-for="pack in CHAT_STICKER_PACKS"
        :key="pack.id"
        type="button"
        class="tab-btn"
        :class="{ active: activeTab === pack.id }"
        :title="pack.name"
        @click="activeTab = pack.id"
      >
        {{ pack.icon }}
      </button>
    </div>

    <div v-if="activeTab === 'emoji'" class="picker-body">
      <div class="category-tabs">
        <button
          v-for="(category, index) in EMOJI_CATEGORIES"
          :key="category.name"
          type="button"
          class="category-btn"
          :class="{ active: activeCategory === index }"
          :title="category.name"
          @click="activeCategory = index"
        >
          {{ category.icon }}
        </button>
      </div>
      <div class="emoji-grid">
        <button
          v-for="emoji in EMOJI_CATEGORIES[activeCategory].emojis"
          :key="emoji"
          type="button"
          class="emoji-item"
          @click="emit('pick-emoji', emoji)"
        >
          {{ emoji }}
        </button>
      </div>
    </div>

    <div v-else class="picker-body">
      <div class="sticker-grid">
        <button
          v-for="item in activeStickerPack?.stickers ?? []"
          :key="item.id"
          type="button"
          class="sticker-item"
          :title="item.label"
          @click="emit('pick-sticker', item)"
        >
          <img :src="item.imageUrl" :alt="item.label" loading="lazy" />
          <span>{{ item.label }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  CHAT_STICKER_PACKS,
  EMOJI_CATEGORIES,
  type ChatSticker,
} from '../../constants/chatEmojis'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'pick-emoji': [emoji: string]
  'pick-sticker': [sticker: ChatSticker]
}>()

const activeTab = ref<'emoji' | string>('emoji')
const activeCategory = ref(0)

const activeStickerPack = computed(() =>
  CHAT_STICKER_PACKS.find((pack) => pack.id === activeTab.value),
)

watch(activeTab, () => {
  activeCategory.value = 0
})
</script>

<style scoped>
.emoji-picker {
  position: absolute;
  left: 0;
  right: 0;
  bottom: calc(100% + 8px);
  box-sizing: border-box;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
  overflow: hidden;
  z-index: 20;
}
.picker-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 10px;
  border-bottom: 1px solid #eef2f7;
  background: #f8fafc;
}
.tab-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  transition: 0.15s ease;
}
.tab-btn:hover,
.tab-btn.active {
  background: #eff6ff;
}
.picker-body {
  padding: 10px;
  max-height: min(320px, 48vh);
  overflow-y: auto;
  overflow-x: hidden;
}
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}
.category-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: #f1f5f9;
  font-size: 18px;
  cursor: pointer;
  flex-shrink: 0;
}
.category-btn.active {
  background: #dbeafe;
}
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 2px;
}
.emoji-item {
  width: 100%;
  aspect-ratio: 1;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
  transition: 0.12s ease;
}
.emoji-item:hover {
  background: #f1f5f9;
  transform: scale(1.08);
}
.sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}
.sticker-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
  padding: 8px 4px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: 0.15s ease;
}
.sticker-item:hover {
  border-color: #bfdbfe;
  background: #f8fbff;
  transform: translateY(-1px);
}
.sticker-item img {
  width: 56px;
  height: 56px;
  object-fit: contain;
}
.sticker-item span {
  max-width: 100%;
  font-size: 11px;
  color: #64748b;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
