const routes = [
    {
        path: '/home',
        component: viewNav,
        meta: { title: '自述文件' },
        children: [
            {
                path: '',
                component: viewNav,
                meta: { title: '主页' }
            }
        ]
    }
];

const router = new VueRouter({
    // mode:'history',
    routes
});