<template>
  <a-flex align="center" vertical gap="large">
    <a-button @click="onOpenGroupModal" type="primary" size="small">新建分组</a-button>
    <a-tree
      :fieldNames="{ title: 'name', key: 'id' }"
      :show-icon="true"
      block-node
      :tree-data="groupList"
      v-model:selected-keys="selectedKeys"
      @select="onSelect"
    >
      <template #title="{ id, name }">
        <a-dropdown :trigger="['contextmenu']">
          <span style="font-size: 16px">{{ name }}</span>
          <template #overlay>
            <a-menu @click="({ key: menuKey }) => onContextMenuClick(id, menuKey)">
              <a-menu-item key="edit">编辑</a-menu-item>
              <a-menu-item key="delete">删除</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
    </a-tree>
  </a-flex>
  <group-form-modal ref="groupFormModalRef" @onSaveSuccess="onSaveSuccess"/>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import api from '@/utils/api.ts'
import { group } from '@/api/leopard/group.ts'

import GroupFormModal from '@/views/leopard/shorturl/components/group-form-modal/index.vue'
import {message, Modal} from 'ant-design-vue'
const emit=defineEmits(['onSelectGroup','onChange'])
const groupList = ref([])
const selectedKeys = ref<string[]>(['1' ]);
const loadGroups = async () => {
  const res = await (<any>api.action(group.list))
  groupList.value = res.records || []
}
loadGroups()
//选中节点
const onSelect = (selectedKeys: any) => {
  emit('onSelectGroup',selectedKeys[0])
}
const onSaveSuccess=async ()=>{
  await loadGroups()
  emit('onChange')
}
const onContextMenuClick = (treeKey: string, menuKey: string) => {
  if (treeKey=='1'){
    message.warn("默认分组不能编辑和删除")
    return
  }
  if (menuKey == 'edit') {
    onOpenGroupEdit({id:treeKey})
  }
  if (menuKey == 'delete') {
    Modal.confirm({
      title: '你确定删除该分组?',
      content: '删除后短链接自动迁移到默认分组',
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
       api.action(group.del,{},[treeKey]).then(res=>{
         message.success("删除成功")
         loadGroups()
       })
      },
    })
  }
}

const groupFormModalRef = ref()
const onOpenGroupModal = () => {
  groupFormModalRef.value.showModal()
}
const onOpenGroupEdit = (data: object) => {
  groupFormModalRef.value.showModal(data)
}
</script>
