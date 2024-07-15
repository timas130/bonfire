use c_core::page_info::Paginated;
use c_core::prelude::chrono::{DateTime, NaiveDate, Utc};
use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::auth::{Auth, AuthServiceClient};
use c_core::services::level::{Level, LevelServiceClient};
use c_core::services::profile::{
    AccountCustomization, Badge, NicknameColorPreset, ProfileCustomization, ProfileError,
    ProfileService,
};
use c_core::{host_tcp, ServiceBase};

mod methods;

#[derive(Clone)]
pub struct ProfileServer {
    base: ServiceBase,
    level: LevelServiceClient,
    // auth is required for a SINGLE edge case. duh
    auth: AuthServiceClient,
}
impl ProfileServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        let level = Level::client_tcp(base.config.ports.level).await?;
        let auth = Auth::client_tcp(base.config.ports.auth).await?;

        Ok(Self { base, level, auth })
    }

    host_tcp!(profile);
}

//noinspection RsSortImplTraitMembers
#[tarpc::server]
impl ProfileService for ProfileServer {
    async fn get_account_customization(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<AccountCustomization, ProfileError> {
        self._get_account_customization(user_id).await
    }

    async fn get_profile_customization(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<ProfileCustomization, ProfileError> {
        self._get_profile_customization(user_id).await
    }

    async fn set_nickname_color(
        self,
        _: Context,
        user_id: i64,
        preset: Option<NicknameColorPreset>,
    ) -> Result<AccountCustomization, ProfileError> {
        self._set_nickname_color(user_id, preset).await
    }

    async fn get_user_badges(
        self,
        _: Context,
        user_id: i64,
        before: Option<DateTime<Utc>>,
    ) -> Result<Paginated<Badge, DateTime<Utc>>, ProfileError> {
        self._get_user_badges(user_id, before).await
    }

    async fn get_badge(self, _: Context, badge_id: i64) -> Result<Option<Badge>, ProfileError> {
        self._get_badge(badge_id).await
    }

    async fn admin_put_badge(self, _: Context, badge: Badge) -> Result<Badge, ProfileError> {
        self._admin_put_badge(badge).await
    }

    async fn admin_remove_badge(self, _: Context, badge_id: i64) -> Result<Badge, ProfileError> {
        self._admin_remove_badge(badge_id).await
    }

    async fn set_active_badge(
        self,
        _: Context,
        user_id: i64,
        badge_id: Option<i64>,
    ) -> Result<AccountCustomization, ProfileError> {
        self._set_active_badge(user_id, badge_id).await
    }

    async fn set_badge_shelf(
        self,
        _: Context,
        user_id: i64,
        badges: [Option<i64>; 4],
    ) -> Result<ProfileCustomization, ProfileError> {
        self._set_badge_shelf(user_id, badges).await
    }

    async fn set_show_badge_shelf(
        self,
        _: Context,
        user_id: i64,
        show: bool,
    ) -> Result<ProfileCustomization, ProfileError> {
        self._set_show_badge_shelf(user_id, show).await
    }

    async fn admin_set_nickname_color(
        self,
        _: Context,
        user_id: i64,
        color: Option<u32>,
    ) -> Result<AccountCustomization, ProfileError> {
        self._admin_set_nickname_color(user_id, color).await
    }

    async fn set_birthday(
        self,
        _: Context,
        user_id: i64,
        birthday: NaiveDate,
    ) -> Result<(), ProfileError> {
        self._set_birthday(user_id, birthday).await
    }

    async fn get_birthday(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<Option<NaiveDate>, ProfileError> {
        self._get_birthday(user_id).await
    }

    async fn is_age_at_least(
        self,
        _: Context,
        user_id: i64,
        age: u32,
    ) -> Result<Option<bool>, ProfileError> {
        self._is_age_at_least(user_id, age).await
    }
}
