import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminGoodsPage from './AdminGoodsPage.vue'

const mocks = vi.hoisted(() => ({
  queryAdminGoods: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { goodsList: [{ goodsId: '9890001', goodsName: '测试商品', originalPrice: 10, status: 0, coverImageUrl: 'https://cdn.example.com/1.png', imageList: [{ imageId: 1, imageUrl: 'https://cdn.example.com/1.png', sortOrder: 1 }] }] }
  }),
  createAdminGoods: vi.fn().mockResolvedValue({ code: '0000', data: { goodsId: '9890002' } }),
  updateAdminGoods: vi.fn().mockResolvedValue({ code: '0000' }),
  updateAdminGoodsStatus: vi.fn().mockResolvedValue({ code: '0000' }),
  uploadAdminGoodsImage: vi.fn().mockResolvedValue({ code: '0000', data: { imageId: 2, imageUrl: 'https://cdn.example.com/2.png', sortOrder: 2 } }),
  deleteAdminGoodsImage: vi.fn().mockResolvedValue({ code: '0000' })
}))

vi.mock('../../lib/admin', () => mocks)

describe('AdminGoodsPage', () => {
  it('renders goods table rows', async () => {
    const wrapper = mount(AdminGoodsPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('测试商品')
    expect(wrapper.text()).toContain('新增商品')
    expect(wrapper.find('input[placeholder="请输入商品ID"]').exists()).toBe(false)
    expect(wrapper.find('img[alt="测试商品"]').attributes('src')).toBe('https://cdn.example.com/1.png')
  })

  it('uploads and deletes product images for selected goods', async () => {
    const wrapper = mount(AdminGoodsPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.get('button[data-test="edit-goods-9890001"]').trigger('click')

    const input = wrapper.get('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [new File(['x'], 'new.png', { type: 'image/png' })] })
    await input.trigger('change')

    expect(mocks.uploadAdminGoodsImage).toHaveBeenCalledWith('9890001', expect.any(File))

    await wrapper.get('button[data-test="delete-image-1"]').trigger('click')
    expect(mocks.deleteAdminGoodsImage).toHaveBeenCalledWith('9890001', 1)
  })
})
