const routes = [
    {
        path: '/',
        redirect: '/home'
    },
    {
        path: '/home',
        component: home,
        meta: {
            title: '系统概览',
            breadcrumb:[
                {
                    name:'系统概览',
                    path:'/home'
                }
            ]
        }
    },
    {
        path: '/home1',
        component: home,
        meta: { title: '系统概览' }
    },
    {
        path: '/home2',
        component: home,
        meta: { title: '系统概览' }
    },
    {
        path: '/timer',
        component: viewNav,
        meta: { title: '定时任务' }
    }
];

const router = new VueRouter({
    // mode:'history',
    routes
});