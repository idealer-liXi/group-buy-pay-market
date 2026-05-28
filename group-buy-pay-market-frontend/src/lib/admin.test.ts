import { describe, expect, it, vi } from 'vitest'
import { addAdminTagMember, createAdminGoods, createAdminTag, deleteAdminGoodsImage, queryAdminTags, queryAdminUsers, removeAdminTagMember, uploadAdminGoodsImage } from './admin'
import { http } from './http'

vi.mock('./admin-auth', () => ({ getAdminToken: () => 'admin-token' }))
vi.mock('./http', () => ({
  http: {
    get: vi.fn().mockResolvedValue({ data: { code: '0000', data: {} } }),
    post: vi.fn().mockResolvedValue({ data: { code: '0000', data: { goodsId: '9890005' } } }),
    delete: vi.fn().mockResolvedValue({ data: { code: '0000' } })
  }
}))

describe('admin goods image api', () => {
  it('createAdminGoods returns generated goodsId', async () => {
    const result = await createAdminGoods({ goodsName: '新商品', originalPrice: 19.9 })

    expect(result.data.goodsId).toBe('9890005')
  })

  it('uploadAdminGoodsImage posts multipart form data', async () => {
    const file = new File(['x'], 'a.png', { type: 'image/png' })

    await uploadAdminGoodsImage('9890001', file)

    const [url, formData, config] = vi.mocked(http.post).mock.calls[1]
    expect(url).toBe('/api/v1/admin/goods/9890001/images')
    expect(formData).toBeInstanceOf(FormData)
    expect(config?.headers?.['X-Admin-Token']).toBe('admin-token')
  })

  it('deleteAdminGoodsImage calls image endpoint', async () => {
    await deleteAdminGoodsImage('9890001', 1)

    expect(http.delete).toHaveBeenCalledWith('/api/v1/admin/goods/9890001/images/1', { headers: { 'X-Admin-Token': 'admin-token' } })
  })

  it('queryAdminUsers calls users endpoint', async () => {
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: '0000', data: { userList: [] } } })

    await queryAdminUsers('u1')

    expect(http.get).toHaveBeenCalledWith('/api/v1/admin/users', { headers: { 'X-Admin-Token': 'admin-token' }, params: { keyword: 'u1' } })
  })

  it('queryAdminTags calls tags endpoint', async () => {
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: '0000', data: { tagList: [] } } })

    await queryAdminTags()

    expect(http.get).toHaveBeenCalledWith('/api/v1/admin/tags', { headers: { 'X-Admin-Token': 'admin-token' } })
  })

  it('createAdminTag posts tag payload', async () => {
    await createAdminTag({ tagId: 'T001', tagName: '新人', tagDesc: '新人标签' })

    expect(http.post).toHaveBeenCalledWith('/api/v1/admin/tags', { tagId: 'T001', tagName: '新人', tagDesc: '新人标签' }, { headers: { 'X-Admin-Token': 'admin-token' } })
  })

  it('admin tag member helpers call member endpoints', async () => {
    await addAdminTagMember('T001', 'u1')
    await removeAdminTagMember('T001', 'u1')

    expect(http.post).toHaveBeenCalledWith('/api/v1/admin/tags/T001/members', { userId: 'u1' }, { headers: { 'X-Admin-Token': 'admin-token' } })
    expect(http.delete).toHaveBeenCalledWith('/api/v1/admin/tags/T001/members/u1', { headers: { 'X-Admin-Token': 'admin-token' } })
  })
})
