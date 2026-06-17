<template>
  <section class="product-create-page">
    <div class="page-head">
      <div>
        <h1>{{ isEditing ? '编辑商品' : '发布商品' }}</h1>
        <p>{{ isEditing ? '修改草稿或审核未通过的商品，完善后可以重新提交审核。' : '商品提交后进入待审核状态，审核通过后才会出现在搜索结果中。' }}</p>
      </div>
      <el-button :icon="ArrowLeft" @click="$router.push(isEditing ? '/products/mine' : '/products')">返回列表</el-button>
    </div>

    <div class="form-grid">
      <el-form ref="formRef" :model="form" :rules="activeRules" label-position="top" class="publish-form">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="例如：九成新高等数学教材" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="描述成色、配件、取货位置等信息"
          />
        </el-form-item>

        <div class="two-col">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" filterable>
              <el-option v-for="category in flatCategories" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品标签" prop="tagIds">
            <el-select v-model="form.tagIds" placeholder="请选择标签" multiple filterable>
              <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
            </el-select>
          </el-form-item>
        </div>

        <div class="two-col">
          <el-form-item label="成色" prop="conditionLevel">
            <el-select v-model="form.conditionLevel" placeholder="请选择成色">
              <el-option label="全新" value="NEW" />
              <el-option label="九成新" value="LIKE_NEW" />
              <el-option label="良好" value="GOOD" />
              <el-option label="普通" value="NORMAL" />
            </el-select>
          </el-form-item>
        </div>

        <div class="two-col">
          <el-form-item label="现价" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="原价" prop="originalPrice">
            <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" :step="1" controls-position="right" />
          </el-form-item>
        </div>

        <div class="two-col">
          <el-form-item label="校区" prop="campus">
            <el-select v-model="form.campus" placeholder="请选择校区">
              <el-option label="主校区" value="主校区" />
              <el-option label="雄安校区" value="雄安校区" />
            </el-select>
          </el-form-item>
          <el-form-item label="交易方式" prop="tradeModes">
            <el-checkbox-group v-model="form.tradeModes">
              <el-checkbox label="MEETUP">线下面交</el-checkbox>
              <el-checkbox label="LOCKER">柜机中转</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </div>

        <el-form-item label="商品图片" prop="imageUrls">
          <div class="image-uploader">
            <input ref="fileInputRef" type="file" accept="image/*" multiple hidden @change="handleImageSelect" />
            <el-button :icon="Upload" :loading="uploadingImage" @click="fileInputRef?.click()">上传本地图片</el-button>
            <div v-if="form.imageUrls.length" class="preview-grid">
              <div v-for="(url, index) in form.imageUrls" :key="`${url}-${index}`" class="preview-item">
                <img :src="resolveMediaUrl(url)" alt="商品图片预览" />
                <el-button circle size="small" :icon="Delete" @click="removeImage(index)" />
              </div>
            </div>
            <p class="upload-tip">图片会先上传到后端，发布商品时只提交返回的图片地址。</p>
          </div>
        </el-form-item>

        <div class="submit-row">
          <el-button type="primary" :loading="submitting" @click="submit('PENDING_REVIEW')">提交审核</el-button>
          <el-button :loading="submitting" @click="submit('DRAFT')">保存草稿</el-button>
          <el-button @click="resetForm">清空</el-button>
        </div>
      </el-form>

      <aside class="ai-panel">
        <h2>AI 建议</h2>
        <p>根据标题和描述给出分类、标签和价格区间建议。</p>
        <el-button type="primary" :icon="MagicStick" :loading="suggesting" @click="requestSuggestion">生成建议</el-button>

        <el-empty v-if="!suggestion" description="暂无建议" />
        <div v-else class="suggestion">
          <div>
            <span>建议分类</span>
            <strong>{{ suggestion.suggestedCategoryName || '未匹配' }}</strong>
          </div>
          <div>
            <span>建议价格</span>
            <strong>¥{{ money(suggestion.suggestedMinPrice) }} - ¥{{ money(suggestion.suggestedMaxPrice) }}</strong>
          </div>
          <div>
            <span>标签</span>
            <div class="tag-line">
              <el-tag v-for="tag in suggestion.suggestedTags" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
          </div>
          <el-button :icon="Check" @click="applySuggestion">应用建议</el-button>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Check, Delete, MagicStick, Upload } from '@element-plus/icons-vue'
import {
  createProduct,
  getProduct,
  listCategories,
  listTags,
  suggestProduct,
  updateProduct,
  uploadProductImage,
  type AiProductSuggestion,
  type CategoryItem,
  type ProductPayload,
  type TagItem,
} from '../../api/product'
import { useAuthStore } from '../../stores/auth'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const categories = ref<CategoryItem[]>([])
const tags = ref<TagItem[]>([])
const submitting = ref(false)
const suggesting = ref(false)
const uploadingImage = ref(false)
const suggestion = ref<AiProductSuggestion | null>(null)
const fileInputRef = ref<HTMLInputElement>()
const submitMode = ref<'PENDING_REVIEW' | 'DRAFT'>('PENDING_REVIEW')
const editingId = computed(() => {
  const value = Number(route.params.id)
  return Number.isInteger(value) && value > 0 ? value : undefined
})
const isEditing = computed(() => editingId.value !== undefined)

type ProductForm = Omit<ProductPayload, 'categoryId' | 'title' | 'description' | 'conditionLevel' | 'campus' | 'tradeModes' | 'imageUrls' | 'tagIds'> & {
  categoryId?: number
  title: string
  description: string
  conditionLevel: string
  campus: string
  tradeModes: string[]
  imageUrls: string[]
  tagIds: number[]
}

const form = reactive<ProductForm>({
  categoryId: undefined,
  title: '',
  description: '',
  price: 1,
  originalPrice: undefined,
  conditionLevel: 'LIKE_NEW',
  campus: '',
  tradeModes: ['MEETUP'],
  imageUrls: [],
  tagIds: [],
})

const reviewRules: FormRules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  categoryId: [{ required: true, type: 'number', min: 1, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, type: 'number', min: 0.01, message: '请输入有效价格', trigger: 'change' }],
  originalPrice: [{ required: true, type: 'number', min: 0.01, message: '请输入有效原价', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  campus: [{ required: true, message: '请选择校区', trigger: 'change' }],
  tradeModes: [{ required: true, type: 'array', min: 1, message: '请选择交易方式', trigger: 'change' }],
  imageUrls: [{ required: true, type: 'array', min: 1, message: '请至少上传一张商品图片', trigger: 'change' }],
  tagIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个商品标签', trigger: 'change' }],
}

const flatCategories = computed(() => flattenCategories(categories.value))
const activeRules = computed<FormRules>(() => (submitMode.value === 'DRAFT' ? {} : reviewRules))

onMounted(async () => {
  if (!auth.isLoggedIn) {
    await router.push({ path: '/login', query: { redirect: '/products/new' } })
    return
  }
  if (!auth.isVerified) {
    await router.push({ path: '/verify', query: { redirect: '/products/new' } })
    return
  }
  await loadMeta()
  if (editingId.value) {
    await loadProduct(editingId.value)
  }
})

async function loadProduct(id: number) {
  try {
    const response = await getProduct(id)
    if (response.code !== 0) throw new Error(response.message)
    const product = response.data
    if (product.status !== 'DRAFT' && product.status !== 'REVIEW_REJECTED') {
      ElMessage.warning('只有草稿或审核未通过的商品可以编辑')
      await router.push('/products/mine')
      return
    }
    form.categoryId = product.categoryId
    form.title = product.title ?? ''
    form.description = product.description ?? ''
    form.price = product.price
    form.originalPrice = product.originalPrice
    form.conditionLevel = product.conditionLevel ?? ''
    form.campus = product.campus ?? ''
    form.tradeModes = [...(product.tradeModes ?? [])]
    form.imageUrls = [...(product.imageUrls ?? [])]
    form.tagIds = [...(product.tagIds ?? [])]
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载商品失败'))
    await router.push('/products/mine')
  }
}

async function loadMeta() {
  try {
    const [categoryResponse, tagResponse] = await Promise.all([listCategories(), listTags()])
    if (categoryResponse.code === 0) categories.value = categoryResponse.data
    if (tagResponse.code === 0) tags.value = tagResponse.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载分类标签失败'))
  }
}

async function requestSuggestion() {
  if (!form.title.trim()) {
    ElMessage.warning('请先填写标题')
    return
  }
  suggesting.value = true
  try {
    const response = await suggestProduct({
      title: form.title,
      description: form.description,
      conditionLevel: form.conditionLevel,
    })
    if (response.code !== 0) throw new Error(response.message)
    suggestion.value = response.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '生成 AI 建议失败'))
  } finally {
    suggesting.value = false
  }
}

function applySuggestion() {
  if (!suggestion.value) return
  if (suggestion.value.suggestedCategoryId) form.categoryId = suggestion.value.suggestedCategoryId
  const matchedTagIds = tags.value
    .filter((tag) => suggestion.value?.suggestedTags.includes(tag.name))
    .map((tag) => tag.id)
  if (matchedTagIds.length) form.tagIds = Array.from(new Set([...(form.tagIds ?? []), ...matchedTagIds]))
  if (suggestion.value.suggestedMinPrice && suggestion.value.suggestedMaxPrice) {
    form.price = Number(((suggestion.value.suggestedMinPrice + suggestion.value.suggestedMaxPrice) / 2).toFixed(2))
  }
  ElMessage.success('已应用 AI 建议')
}

async function submit(status: 'PENDING_REVIEW' | 'DRAFT') {
  if (!formRef.value) return
  submitMode.value = status
  await nextTick()
  if (status === 'PENDING_REVIEW') {
    await formRef.value.validate()
  } else {
    formRef.value.clearValidate()
  }
  if (status === 'PENDING_REVIEW' && !form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  submitting.value = true
  try {
    const payload: ProductPayload = {
      ...form,
      categoryId: form.categoryId,
      imageUrls: form.imageUrls?.map((url) => url.trim()).filter(Boolean),
      tagIds: form.tagIds,
      status,
      campus: form.campus?.trim() || undefined,
      title: form.title?.trim() || undefined,
      description: form.description?.trim() || undefined,
    }
    const response = editingId.value
      ? await updateProduct(editingId.value, payload)
      : await createProduct(payload)
    if (response.code !== 0) throw new Error(response.message)
    ElMessage.success(status === 'DRAFT' ? '草稿已保存' : isEditing.value ? '商品已修改并提交审核' : '商品已提交审核')
    await router.push('/products/mine')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '发布失败'))
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.categoryId = undefined
  form.price = 1
  form.originalPrice = undefined
  form.conditionLevel = 'LIKE_NEW'
  form.campus = ''
  form.tradeModes = ['MEETUP']
  form.imageUrls = []
  form.tagIds = []
  suggestion.value = null
}

function removeImage(index: number) {
  form.imageUrls.splice(index, 1)
  formRef.value?.validateField('imageUrls')
}

async function handleImageSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (!files.length) return

  uploadingImage.value = true
  try {
    let successCount = 0
    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        ElMessage.warning(`${file.name} 不是图片文件`)
        continue
      }
      const response = await uploadProductImage(file)
      if (response.code !== 0) throw new Error(response.message)
      form.imageUrls.push(response.data.url)
      successCount += 1
    }
    formRef.value?.validateField('imageUrls')
    if (successCount > 0) ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '图片上传失败'))
  } finally {
    uploadingImage.value = false
    input.value = ''
  }
}

function flattenCategories(items: CategoryItem[]): CategoryItem[] {
  const seen = new Set<string>()
  return items
    .flatMap((item) => [item, ...flattenCategories(item.children ?? [])])
    .filter((item) => {
      if (seen.has(item.name)) return false
      seen.add(item.name)
      return true
    })
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}
</script>

<style scoped>
.product-create-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.page-head h1 {
  margin: 0;
  font-size: 26px;
}
.page-head p {
  margin: 6px 0 0;
  color: #64748b;
}
.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}
.publish-form,
.ai-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.two-col :deep(.el-input-number) {
  width: 100%;
}
.image-uploader {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}
.preview-item {
  position: relative;
  aspect-ratio: 4 / 3;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #f8fafc;
}
.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.preview-item .el-button {
  position: absolute;
  top: 6px;
  right: 6px;
}
.upload-tip {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}
.submit-row {
  display: flex;
  gap: 10px;
}
.ai-panel h2 {
  margin: 0 0 8px;
  font-size: 20px;
}
.ai-panel p {
  margin: 0 0 14px;
  color: #64748b;
  line-height: 1.6;
}
.suggestion {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.suggestion span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 4px;
}
.tag-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
@media (max-width: 900px) {
  .form-grid,
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>
