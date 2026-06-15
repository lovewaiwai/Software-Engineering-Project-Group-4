export interface EmojiCategory {
  name: string
  icon: string
  emojis: string[]
}

export interface ChatSticker {
  id: string
  label: string
  imageUrl: string
}

export interface ChatStickerPack {
  id: string
  name: string
  icon: string
  stickers: ChatSticker[]
}

/** 常用 Unicode 表情，按分类组织 */
export const EMOJI_CATEGORIES: EmojiCategory[] = [
  {
    name: '笑脸',
    icon: '😀',
    emojis: [
      '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
      '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',
    ],
  },
  {
    name: '手势',
    icon: '👍',
    emojis: [
      '👍', '👎', '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘', '👌', '🤌', '🤏', '👈', '👉', '👆',
      '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤙', '💪', '🙏', '👏', '🤝', '💅', '🤳', '✍️', '🙌',
    ],
  },
  {
    name: '动物',
    icon: '🐱',
    emojis: [
      '🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐸', '🐵', '🐔',
      '🐧', '🐦', '🐤', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞',
    ],
  },
  {
    name: '食物',
    icon: '🍔',
    emojis: [
      '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝',
      '🍅', '🍆', '🥑', '🥦', '🥬', '🥒', '🌶️', '🫑', '🌽', '🥕', '🍔', '🍟', '🍕', '🌭', '🥪', '🌮',
    ],
  },
  {
    name: '校园',
    icon: '📚',
    emojis: [
      '📚', '📖', '📝', '✏️', '📌', '📎', '🖇️', '📐', '📏', '🎒', '🏫', '🎓', '💻', '🖥️', '⌨️', '🖱️',
      '☕', '🧋', '🍜', '🏃', '🚲', '⚽', '🏀', '🎸', '🎮', '🎬', '📷', '💡', '🔔', '⏰', '📅', '✅',
    ],
  },
  {
    name: '符号',
    icon: '❤️',
    emojis: [
      '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖',
      '💘', '💝', '💟', '⭐', '🌟', '✨', '💫', '🔥', '💯', '✅', '❌', '❓', '❗', '💤', '🎉', '🎊',
    ],
  },
]

const DEFAULT_STICKER_BASE_URL = 'https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72'

function stickerBaseUrl(): string {
  const configured = import.meta.env.VITE_STICKER_BASE_URL as string | undefined
  return (configured && configured.trim() ? configured : DEFAULT_STICKER_BASE_URL).replace(/\/+$/, '')
}

function twemojiAssetName(emoji: string): string {
  const codePoints = [...emoji]
    .map((char) => char.codePointAt(0)?.toString(16))
    .filter(Boolean)
    .join('-')
  return `${codePoints}.png`
}

function twemojiUrl(emoji: string): string {
  return `${stickerBaseUrl()}/${twemojiAssetName(emoji)}`
}

function sticker(id: string, label: string, emoji: string): ChatSticker {
  return { id, label, imageUrl: twemojiUrl(emoji) }
}

/** 内置 Campus 表情包（WeChat 风格大图表情） */
export const CHAT_STICKER_PACKS: ChatStickerPack[] = [
  {
    id: 'campus',
    name: 'Campus',
    icon: '🎓',
    stickers: [
      sticker('campus:hi', '你好', '👋'),
      sticker('campus:thanks', '谢谢', '🙏'),
      sticker('campus:ok', 'OK', '👌'),
      sticker('campus:good', '赞', '👍'),
      sticker('campus:clap', '鼓掌', '👏'),
      sticker('campus:heart', '比心', '🥰'),
      sticker('campus:laugh', '大笑', '🤣'),
      sticker('campus:cry', '哭了', '😭'),
      sticker('campus:angry', '生气', '😤'),
      sticker('campus:think', '思考', '🤔'),
      sticker('campus:sleep', '困了', '😴'),
      sticker('campus:party', '庆祝', '🎉'),
      sticker('campus:book', '学习中', '📚'),
      sticker('campus:coffee', '喝咖啡', '☕'),
      sticker('campus:bike', '骑车', '🚲'),
      sticker('campus:deal', '成交', '🤝'),
    ],
  },
  {
    id: 'mood',
    name: '心情',
    icon: '😊',
    stickers: [
      sticker('mood:happy', '开心', '😄'),
      sticker('mood:shy', '害羞', '😊'),
      sticker('mood:cool', '酷', '😎'),
      sticker('mood:love', '喜欢', '😍'),
      sticker('mood:wink', '眨眼', '😉'),
      sticker('mood:sweat', '尴尬', '😅'),
      sticker('mood:shock', '震惊', '😱'),
      sticker('mood:facepalm', '无语', '🤦'),
      sticker('mood:pray', '拜托', '🙏'),
      sticker('mood:strong', '加油', '💪'),
      sticker('mood:fire', '厉害', '🔥'),
      sticker('mood:100', '满分', '💯'),
    ],
  },
]

const stickerMap = new Map<string, ChatSticker>()
for (const pack of CHAT_STICKER_PACKS) {
  for (const item of pack.stickers) {
    stickerMap.set(item.id, item)
  }
}

export function resolveSticker(stickerId?: string | null): ChatSticker | null {
  if (!stickerId) return null
  return stickerMap.get(stickerId) ?? null
}

export function stickerPreviewLabel(stickerId?: string | null): string {
  const stickerItem = resolveSticker(stickerId)
  return stickerItem ? `[${stickerItem.label}]` : '[表情]'
}
