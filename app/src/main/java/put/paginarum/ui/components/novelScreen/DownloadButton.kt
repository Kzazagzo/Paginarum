package put.paginarum.ui.components.novelScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.RawValue
import put.paginarum.R
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.database.novel.ChapterTextEntity
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.util.DataStatus

@Composable
fun DownloadButton(
    provider: @RawValue ProviderApi,
    library: LibraryScreenModel,
    inLibrary: Boolean,
    status: DataStatus<List<ChapterTextEntity>>,
    novelUrl: String,
    chapterUrl: String,
    modifier: Modifier,
) {
    when (status) {
        is DataStatus.Error -> {}
        DataStatus.Loading -> {}
        is DataStatus.Success -> {
            if (inLibrary) {
                var downloaded by remember {
                    mutableStateOf(
                        status.data.any {
                            it.chapterUrl ==
                                chapterUrl
                        },
                    )
                }
                if (downloaded) {
                    IconButton(
                        onClick = {
                            library.chapterTextDelete(chapterUrl)
                            downloaded = false
                        },
                        modifier =
                            modifier
                                .padding(end = 16.dp),
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    id = R.drawable.baseline_download_done_24,
                                ),
                            contentDescription = "download",
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            library.downloadChapterText(provider, novelUrl, chapterUrl)
                            downloaded = true
                        },
                        modifier =
                            modifier
                                .padding(end = 16.dp),
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    id = R.drawable.baseline_download_24,
                                ),
                            contentDescription = "download",
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = { },
                    modifier =
                        modifier
                            .padding(end = 16.dp),
                ) {
                    Icon(
                        painter =
                            painterResource(
                                id = R.drawable.baseline_download_off_24,
                            ),
                        contentDescription = "downloadoff",
                    )
                }
            }
        }
    }
}
