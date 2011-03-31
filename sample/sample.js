osapi.people.get({ userId: '@viewer' }).execute(function(response) {

    // ユーザーID
    // 例）org001:sample1
    var userId = response.id;

    // 表示名（氏名）
    // 例）木村 一郎
    var displayName = response.displayName;

    // ...
});

osapi.people.getViewer().execute(function(response) {
    // ...
});


osapi.people.get({ userId: '@viewer', groupId: '@all' }).execute(function(response) {

    var users = response.list;

    for (var i in users) {

        // ユーザーID
        // 例）org001:sample1
        var userId = users[i].id;

        // 表示名（氏名）
        // 例）木村 一郎
        var displayName = users[i].displayName;

        // ...
    }

    // ...
});

osapi.people.get({ userId: '@viewer', groupId: '@all', startIndex: 10, count: 50 }).execute(function(response) {

    // 全件数
    // 例）100
    var total = response.totalResults;

    // 開始位置
    // 例）10
    var startIndex = response.startIndex;

    // 取得件数
    // 例）50
    var count = response.itemsPerPage;

    var users = response.list;

    for (var i in users) {
        // ユーザーID
        // 例）org001:sample1
        var userId = users[i].id;

        // 表示名（氏名）
        // 例）木村 一郎
        var displayName = users[i].displayName;

        // ...
    }

    // ...

});

osapi.groups.get({ userId: '@viewer' }).execute(function(response) {

    var groups = response.list;

    for (var i in groups) {
        // グループID
        // 例）5031301034848187_23
        var groupId = groups[i].id.groupId;

        // グループ名（部署名）
        // 例）営業部
        var title = groups[i].title;

        // グループ種別（部署、マイグループ）
        // 例）unit
        var type = groups[i].type

        // ...
    }

    // ...
});

osapi.groups.get({ userId: '@viewer', startIndex: 10, count: 50 }).execute(function(response) {

    // 全件数
    // 例）100
    var total = response.totalResults;

    // 開始位置
    // 例）10
    var startIndex = response.startIndex;

    // 取得件数
    // 例）50
    var count = response.itemsPerPage;

    var groups = response.list;

    for (var i in groups) {
        // グループID
        // 例）5031301034848187_23
        var groupId = groups[i].id.groupId;

        // グループ名（部署名）
        // 例）営業部
        var title = groups[i].title;

        // グループ種別（部署、マイグループ）
        // 例）unit
        var type = groups[i].type

        // ...
    }

    // ...
});

osapi.groups.get({ userId: '@viewer' }).execute(function(response) {

    var groups = response.list;

    // グループID
    // 例）5031301034848187_23
    var groupId = groups[0].id.groupId;

    osapi.people.get({ userId: '@viewer', groupId: groupId }).execute(function(response) {

        var users = response.list;

        for (var i in users) {
            // ユーザーID
            // 例）org001:sample1
            var userId = users[i].id;

            // 表示名（氏名）
            // 例）木村 一郎
            var displayName = users[i].displayName;

            // ...
        }

        // ...
    });

});

osapi.activities.create({
    userId: '@viewer', activity: { title: 'アクティビティのタイトル' }
}).execute(function(response) {
    // ...
});


var recipients = [ "org001:sample2","org001:sample2","org001:sample3"];

osapi.activities.create({
    userId: '@viewer', activity: { title: 'アクティビティのタイトル',recipients:recipients }
}).execute(function(response) {
    // ...
});


var recipients = [ "org001:sample1","org001:sample2","org001:sample3"];

osapi.activities.create({
    userId: '@viewer', activity: { title: 'アクティビティのタイトル', recipients:recipients, priority: 1 }
}).execute(function(response) {
    // ...
});

osapi.activities.create({
    userId: '@viewer', activity: { title: 'アクティビティのタイトル', externalId: "2" }
}).execute(function(response) {
    // ...
});

// アクティビティ送信時に受け渡された externalId パラメータ
var externalId = gadgets.views.getParams()['externalId'];

osapi.people.get({ userId: '@viewer', groupId: '@all', filterBy: 'name', filterOp: 'contains', filterValue: '太郎' }).execute(function(response) {
    // ...
});

osapi.people.get({ userId: '@viewer', groupId: '@all', sortBy: 'position', sortOrder: 'descending' }).execute(function(response) {
    // ...
});

osapi.groups.get({ userId: '@viewer', filterBy: 'type', filterOp: 'equals', filterValue: 'mygroup' }).execute(function(response) {
    // ...
});

osapi.groups.get({ userId: '@viewer', sortBy: 'title', sortOrder: 'descending' }).execute(function(response) {
    // ...
});